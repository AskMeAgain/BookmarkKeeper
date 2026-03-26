package io.github.askmeagain.bookmarkkeeper;

import com.intellij.ide.bookmark.BookmarkGroup;
import com.intellij.ide.bookmark.BookmarksManager;
import com.intellij.ide.bookmark.FileBookmark;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import io.github.askmeagain.bookmarkkeeper.dtos.BookmarkContainer;
import io.github.askmeagain.bookmarkkeeper.state.StateData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@State(name = "BookmarkKeeperSettings", storages = @Storage("bookmarkKeeper-2.0.0.xml"))
@Service(Service.Level.PROJECT)
public final class BookmarkKeeperManager implements PersistentStateComponent<StateData> {

  private static final Logger LOG = Logger.getInstance(BookmarkKeeperManager.class);

  private final Project project;
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
    this.state = state;
  }

  public static BookmarkKeeperManager getInstance(Project project) {
    return project.getService(BookmarkKeeperManager.class);
  }

  public void OnContextClosed() {
    var bookmarksManager = BookmarksManager.getInstance(project);

    // Keep metadata for missing bookmarks and deduplicate the combined result.
    state.allBookmarks = Stream.concat(
        bookmarksManager.getBookmarks().stream().map(bookmark -> new BookmarkContainer(bookmarksManager, bookmark)),
        state.notExistingBookmarks.stream()
    ).collect(Collectors.collectingAndThen(
        Collectors.toMap(
            BookmarkKeeperManager::bookmarkKey,
            container -> container,
            (existing, ignored) -> existing,
            LinkedHashMap::new
        ),
        map -> new ArrayList<>(map.values())
    ));
  }

  public void OnContextOpened() {
    var bookmarksManager = BookmarksManager.getInstance(project);

    //clearing the current bookmarks
    bookmarksManager.remove();

    //adding now the bookmarks back
    state.notExistingBookmarks = new ArrayList<>();
    var groups = new HashMap<String, BookmarkGroup>();

    for (var container : state.allBookmarks) {
      if (!bookmarkExists(container)) {
        state.notExistingBookmarks.add(container);
        continue;
      }

      var name = container.groupName;

      if (name == null || name.isBlank()) {
        LOG.warn("Bookmark has no group name, skipping group restore for " + container.bookmark);
        continue;
      }

      var group = groups.computeIfAbsent(name, key -> bookmarksManager.addGroup(name, false));
      group.add(container.bookmark, container.type, container.description);
    }
  }

  private static String bookmarkKey(BookmarkContainer container) {
    var attributes = container.bookmark == null ? null : container.bookmark.getAttributes();
    var attributesKey = attributes == null ? "" : attributes.entrySet().stream()
        .sorted(Entry.comparingByKey())
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .collect(Collectors.joining("|"));

    var bookmarkText = container.bookmark == null ? "" : container.bookmark.toString();
    var type = container.type == null ? "" : container.type.toString();
    var groupName = container.groupName == null ? "" : container.groupName;
    var description = container.description == null ? "" : container.description;

    return groupName + "||" + type + "||" + description + "||" + attributesKey + "||" + bookmarkText;
  }

  private boolean bookmarkExists(BookmarkContainer container) {
    if (container.bookmark instanceof FileBookmark fileBookmark) {
      var file = fileBookmark.getFile();

      return VfsUtilCore.virtualToIoFile(file).exists();
    }

    return true;
  }
}
