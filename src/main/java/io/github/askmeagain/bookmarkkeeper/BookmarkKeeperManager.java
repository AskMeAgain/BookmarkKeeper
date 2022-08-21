package io.github.askmeagain.bookmarkkeeper;

import com.intellij.ide.bookmark.BookmarkType;
import com.intellij.ide.bookmark.BookmarksManager;
import com.intellij.ide.bookmark.Bookmark;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public final class BookmarkKeeperManager {

  private Map<BookmarkType, Bookmark> bookmarks;

  public static BookmarkKeeperManager getInstance() {
    return ApplicationManager.getApplication().getService(BookmarkKeeperManager.class);
  }

  public void saveBookmarks(Project project) {
    var bookmarksManager = BookmarksManager.getInstance(project);

    bookmarks = bookmarksManager.getBookmarks()
        .stream()
        .collect(Collectors.toMap(bookmarksManager::getType, Function.identity()));
  }

  public void loadBookmarks(Project project) {
    var instance = BookmarksManager.getInstance(project);

    instance.remove();

    for (var kv : bookmarks.entrySet()) {
      instance.add(kv.getValue(), kv.getKey());
    }
  }
}