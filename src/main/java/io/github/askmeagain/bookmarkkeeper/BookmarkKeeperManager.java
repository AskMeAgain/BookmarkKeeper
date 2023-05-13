package io.github.askmeagain.bookmarkkeeper;

import com.intellij.ide.bookmark.BookmarkType;
import com.intellij.ide.bookmark.BookmarksManager;
import com.intellij.ide.bookmark.Bookmark;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public final class BookmarkKeeperManager {

  private List<Container> bookmarks;

  public static BookmarkKeeperManager getInstance() {
    return ApplicationManager.getApplication().getService(BookmarkKeeperManager.class);
  }

  public void saveBookmarks(Project project) {
    var bookmarksManager = BookmarksManager.getInstance(project);

    bookmarks = bookmarksManager.getBookmarks()
        .stream()
        .map(bookmark -> new Container(bookmarksManager.getType(bookmark), bookmark))
        .collect(Collectors.toList());
  }

  public void loadBookmarks(Project project) {
    var bookmarksManager = BookmarksManager.getInstance(project);

    bookmarksManager.getBookmarks().forEach(bookmarksManager::remove);

    for (var container : bookmarks) {
      bookmarksManager.add(container.getBookmark(), container.getType());
    }
  }
}