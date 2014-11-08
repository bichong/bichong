/*
* Copyright 2013-2014 the original author or authors.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package zhao.util;


import xi.zhao.wallet.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ImageTextButton1 extends RelativeLayout {
    
    private ImageView imgView;  
    private TextView  textView;

    public ImageTextButton1(Context context) {
        super(context,null);
    }
    
    public ImageTextButton1(Context context,AttributeSet attributeSet) {
        super(context, attributeSet);
        
        LayoutInflater.from(context).inflate(R.layout.img_text_bt, this,true);
        
        this.imgView = (ImageView)findViewById(R.id.imgview);
        this.textView = (TextView)findViewById(R.id.textview);
        
        this.setClickable(true);
        this.setFocusable(true);
    }
    
    public void setImgResource(int resourceID) {
        this.imgView.setImageResource(resourceID);
    }
    
    public void setText(String text) {
        this.textView.setText(text);
    }
    
    public void setTextColor(int color) {
        this.textView.setTextColor(color);
    }
    
    public void setTextSize(float size) {
        this.textView.setTextSize(size);
    }
    
}
