package io.github.askmeagain.bookmarkkeeper;

import com.intellij.ide.bookmark.BookmarkGroup;
import com.intellij.ide.bookmark.BookmarksManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public final class BookmarkKeeperManager {

  private List<BookmarkContainer> bookmarks;

  public static BookmarkKeeperManager getInstance() {
    return ApplicationManager.getApplication().getService(BookmarkKeeperManager.class);
  }

  public void saveBookmarks(Project project) {
    var bookmarksManager = BookmarksManager.getInstance(project);

    bookmarks = bookmarksManager.getBookmarks()
        .stream()
        .map(bookmark -> new BookmarkContainer(bookmarksManager, bookmark))
        .collect(Collectors.toList());
  }

  public void loadBookmarks(Project project) {
    var bookmarksManager = BookmarksManager.getInstance(project);

    bookmarksManager.remove();

    var groups = new HashMap<String, BookmarkGroup>();

    for (var container : bookmarks) {
      var name = container.getGroupName();
      var group = groups.computeIfAbsent(name, k -> bookmarksManager.addGroup(name, false));
      group.add(container.getBookmark(), container.getType(), container.getDescription());
    }
  }
}