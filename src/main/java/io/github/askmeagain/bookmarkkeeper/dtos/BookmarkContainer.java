package io.github.askmeagain.bookmarkkeeper.dtos;

import com.intellij.ide.bookmark.Bookmark;
import com.intellij.ide.bookmark.BookmarkType;
import com.intellij.ide.bookmark.BookmarksManager;

public class BookmarkContainer {

  public BookmarkType type;
  public Bookmark bookmark;
  public String description;
  public String groupName;

  public BookmarkContainer() {
  }

  public BookmarkContainer(BookmarksManager bookmarksManager, Bookmark bookmark) {
    var groups = bookmarksManager.getGroups(bookmark);

    if (!groups.isEmpty()) {
      var bookmarkGroup = groups.getFirst();
      this.groupName = bookmarkGroup.getName();
      this.description = bookmarkGroup.getDescription(bookmark);
    }

    this.bookmark = bookmark;
    this.type = bookmarksManager.getType(bookmark);
  }
}
