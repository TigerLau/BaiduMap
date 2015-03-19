package com.tigerlau.maptest.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

public class DrawableButton extends TextView {

	private static final int COLORDRAWABLE_DIMENSION = 1;
	private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;

	private String text;
	private int textWidth = 0;
	private int textHeight = 0;

	// private int bitmapWidth = 0;
	// private int bitmapHeight = 0;
	// private int start;

	public DrawableButton(Context context) {
		super(context);
	}

	public DrawableButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DrawableButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initText();
	}

	private void initText() {
		text = super.getText().toString();
		initVariable();
	}

	private void initVariable() {
		textWidth = (int) (getPaint().measureText(text));
		final Rect rect = new Rect();
		getPaint().getTextBounds(text, 0, 1, rect);
		textHeight = rect.height();
	}

	public void setText(String text) {
		this.text = text;
		initVariable();
		invalidate();
	}

	/**
	 * 获取TextView内容
	 */
	public String getText() {
		return text;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Drawable[] drawables = getCompoundDrawables();
		if (drawables != null) {
			Drawable drawableLeft = drawables[0];
			if (drawableLeft != null) {
				final int bitmapWidth = drawableLeft.getIntrinsicWidth();
				final int bitmapHeight = drawableLeft.getIntrinsicHeight();
				final int viewHeight = getHeight();
				final int drawablePadding = getCompoundDrawablePadding();
				final int start = (getWidth() - (bitmapWidth + drawablePadding + textWidth)) >> 1;
				// 绘制中间左边图片
				canvas.drawBitmap(getBitmapFromDrawable(drawableLeft), start,
						(viewHeight >> 1) - (bitmapHeight >> 1), getPaint());

				// 绘制中间右边文字
				canvas.drawText(text, start + drawablePadding + bitmapWidth,
						(viewHeight >> 1) + (textHeight >> 1), getPaint());
			}
		}else{
			super.onDraw(canvas);
		}
	}

	/**
	 * 根据Drawable返回Bitmap
	 * 
	 * @param drawable
	 * @return
	 */
	private Bitmap getBitmapFromDrawable(Drawable drawable) {
		if (drawable == null) {
			return null;
		}

		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		try {
			Bitmap bitmap;

			if (drawable instanceof ColorDrawable) {
				bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION,
						COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
			} else {
				bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(), BITMAP_CONFIG);
			}

			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			drawable.draw(canvas);
			return bitmap;
		} catch (OutOfMemoryError e) {
			return null;
		}
	}

}
