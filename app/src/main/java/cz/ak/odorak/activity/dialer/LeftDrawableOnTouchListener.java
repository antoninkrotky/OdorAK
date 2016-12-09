package cz.ak.odorak.activity.dialer;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public abstract class LeftDrawableOnTouchListener implements OnTouchListener {
	Drawable drawable;
	private int fuzz = 10;

	/**
	 * @param keyword
	 */
	public LeftDrawableOnTouchListener(TextView view) {
		super();
		final Drawable[] drawables = view.getCompoundDrawables();
		if (drawables != null && drawables.length == 4)
			this.drawable = drawables[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View,
	 * android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(final View v, final MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN && drawable != null) {
			final int x = (int) event.getX();
			final int y = (int) event.getY();
			final Rect bounds = drawable.getBounds();			
			final int left = v.getLeft();
			final int width = bounds.width();
			final int paddingRight =v.getPaddingRight();
			if (x >= (left - fuzz)
					&& x <= (left + width + paddingRight + fuzz)
					&& y >= (v.getPaddingTop() - fuzz)
					&& y <= (v.getHeight() - v.getPaddingBottom()) + fuzz) {
				return onDrawableTouch(event);
			}
		}
		return false;
	}

	public abstract boolean onDrawableTouch(final MotionEvent event);

}