package io.github.askmeagain.bookmarkkeeper;

import com.intellij.ide.bookmark.Bookmark;
import com.intellij.ide.bookmark.BookmarkType;
import com.intellij.ide.bookmark.BookmarksManager;

public class BookmarkContainer {

  private final BookmarkType type;
  private final Bookmark bookmark;
  private final String description;
  private final String groupName;

  public BookmarkContainer(BookmarksManager bookmarksManager, Bookmark bookmark) {
    var bookmarkGroup = bookmarksManager.getGroups(bookmark).get(0);

    this.bookmark = bookmark;
    this.groupName = bookmarkGroup.getName();
    this.description = bookmarkGroup.getDescription(bookmark);
    this.type = bookmarksManager.getType(bookmark);
  }

  public String getDescription() {
    return description;
  }

  public String getGroupName() {
    return groupName;
  }

  public BookmarkType getType() {
    return type;
  }

  public Bookmark getBookmark() {
    return bookmark;
  }
}
