package io.github.askmeagain.bookmarkkeeper;

import com.intellij.ide.bookmark.Bookmark;
import com.intellij.ide.bookmark.BookmarkGroup;
import com.intellij.ide.bookmark.BookmarksManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import io.github.askmeagain.bookmarkkeeper.dtos.BookmarkContainer;
import io.github.askmeagain.bookmarkkeeper.state.StateData;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@State(name = "BookmarkKeeperSettings", storages = @Storage("bookmarkKeeper-3.0.0.xml"))
@Service(Service.Level.PROJECT)
public final class BookmarkKeeperManager implements PersistentStateComponent<StateData> {

  private static final Logger LOG = Logger.getInstance(BookmarkKeeperManager.class);

  private final Project project;
  private final AtomicLong restoreRequestCounter = new AtomicLong();
  private StateData state = new StateData();

  public BookmarkKeeperManager(Project project) {
    this.project = project;
  }

  @Override
  public StateData getState() {
    return state;
  }

  @Override
  public void loadState(@NotNull StateData state) {
    if (state.allBookmarks == null) {
      state.allBookmarks = new ArrayList<>();
    }
    if (state.notExistingBookmarks == null) {
      state.notExistingBookmarks = new ArrayList<>();
    }
    this.state = state;
  }

  public static BookmarkKeeperManager getInstance(Project project) {
    return project.getService(BookmarkKeeperManager.class);
  }

  public void OnContextClosed() {
    var bookmarksManager = BookmarksManager.getInstance(project);

    // Keep metadata for missing bookmarks and deduplicate the combined result.
    state.allBookmarks = Stream.concat(
        getBookmarks(bookmarksManager)
            .stream()
            .map(bookmark -> new BookmarkContainer(bookmarksManager, bookmark)),
        state.notExistingBookmarks.stream()
    ).collect(Collectors.collectingAndThen(
        Collectors.toMap(
            BookmarkUtils::bookmarkKey,
            container -> container,
            (existing, ignored) -> existing,
            LinkedHashMap::new
        ),
        map -> new ArrayList<>(map.values())
    ));
  }

  private static @NonNull List<Bookmark> getBookmarks(BookmarksManager bookmarksManager) {
    if (bookmarksManager.getBookmarks() == null) {
      return new ArrayList<>();
    }
    return bookmarksManager.getBookmarks();
  }

  public void OnContextOpened() {
    var requestId = restoreRequestCounter.incrementAndGet();
    var bookmarkSnapshot = List.copyOf(state.allBookmarks);

    AppExecutorUtil.getAppExecutorService().execute(() -> {
      var existingBookmarks = new ArrayList<BookmarkContainer>();
      var missingBookmarks = new ArrayList<BookmarkContainer>();

      for (var container : bookmarkSnapshot) {
        if (BookmarkUtils.bookmarkExists(container)) {
          existingBookmarks.add(container);
        } else {
          missingBookmarks.add(container);
        }
      }

      ApplicationManager.getApplication().invokeLater(() -> {
        if (project.isDisposed() || restoreRequestCounter.get() != requestId) {
          return;
        }

        var bookmarksManager = BookmarksManager.getInstance(project);

        // Apply bookmark changes on the UI thread after background file existence checks complete.
        bookmarksManager.remove();

        state.notExistingBookmarks = new ArrayList<>(missingBookmarks);
        var groups = new HashMap<String, BookmarkGroup>();

        for (var container : existingBookmarks) {
          var name = container.groupName;

          if (name == null || name.isBlank()) {
            LOG.warn("Bookmark has no group name, skipping group restore for " + container.bookmark);
            continue;
          }

          var group = groups.computeIfAbsent(name, key -> bookmarksManager.addGroup(name, false));
          group.add(container.bookmark, container.type, container.description);
        }
      }, project.getDisposed());
    });
  }

}
