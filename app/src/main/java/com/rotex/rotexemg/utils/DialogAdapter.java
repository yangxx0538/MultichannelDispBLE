package com.rotex.rotexemg.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rotex.rotexhand.R;


public class DialogAdapter extends BaseAdapter {

  private LayoutInflater layoutInflater;
  private boolean isGrid;

  public DialogAdapter(Context context, boolean isGrid) {
    layoutInflater = LayoutInflater.from(context);
    this.isGrid = isGrid;
  }

  @Override
  public int getCount() {
    return 2;
  }

  @Override
  public Object getItem(int position) {
    return position;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    View view = convertView;

    if (view == null) {
      if (isGrid) {
        view = layoutInflater.inflate(R.layout.simple_grid_item, parent, false);
      } else {
        view = layoutInflater.inflate(R.layout.simple_grid_item, parent, false);
      }

      viewHolder = new ViewHolder();
      viewHolder.textView = (TextView) view.findViewById(R.id.text_view);
      viewHolder.imageView = (ImageView) view.findViewById(R.id.image_view);
      view.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) view.getTag();
    }

    Context context = parent.getContext();
    switch (position) {
      case 0:
        viewHolder.textView.setText(context.getString(R.string.chinese));
        viewHolder.imageView.setImageResource(R.mipmap.wuxinghongqi);
        break;

      default:
        viewHolder.textView.setText(context.getString(R.string.english));
        viewHolder.imageView.setImageResource(R.mipmap.yingguoguoqi);
        break;
    }

    return view;
  }

  static class ViewHolder {
    TextView textView;
    ImageView imageView;
  }
}
