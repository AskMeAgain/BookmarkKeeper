package io.github.askmeagain.bookmarkkeeper;

import com.intellij.ide.bookmark.Bookmark;
import com.intellij.ide.bookmark.BookmarkType;

public class Container {

  private final BookmarkType type;
  private final Bookmark bookmark;

  public Container(BookmarkType type, Bookmark bookmark) {
    this.bookmark = bookmark;
    this.type = type;
  }

  public BookmarkType getType() {
    return type;
  }

  public Bookmark getBookmark() {
    return bookmark;
  }
}
