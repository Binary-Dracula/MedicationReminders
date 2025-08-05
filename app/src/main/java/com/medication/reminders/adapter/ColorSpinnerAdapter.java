package com.medication.reminders.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.medication.reminders.R;
import com.medication.reminders.models.MedicationColor;

/**
 * Custom spinner adapter for medication color selection
 * Displays color options with visual color indicators and localized names
 * Designed for elderly users with accessibility considerations
 */
public class ColorSpinnerAdapter extends BaseAdapter {
    
    private final Context context;
    private final MedicationColor[] colors;
    private final LayoutInflater inflater;
    
    /**
     * Constructor for ColorSpinnerAdapter
     * @param context Android context
     */
    public ColorSpinnerAdapter(Context context) {
        this.context = context;
        this.colors = MedicationColor.getAllColors();
        this.inflater = LayoutInflater.from(context);
    }
    
    @Override
    public int getCount() {
        return colors.length;
    }
    
    @Override
    public MedicationColor getItem(int position) {
        return colors[position];
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent, false);
    }
    
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent, true);
    }
    
    /**
     * Create view for spinner item
     * @param position Item position
     * @param convertView Recycled view
     * @param parent Parent view group
     * @param isDropDown Whether this is for dropdown view
     * @return Configured view
     */
    private View createView(int position, View convertView, ViewGroup parent, boolean isDropDown) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.spinner_color_item, parent, false);
            holder = new ViewHolder();
            holder.colorIndicator = convertView.findViewById(R.id.colorIndicator);
            holder.colorName = convertView.findViewById(R.id.colorName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        MedicationColor color = colors[position];
        
        // Set color name with localized text
        holder.colorName.setText(color.getDisplayName(context));
        
        // Set color indicator
        setColorIndicator(holder.colorIndicator, color);
        
        // Apply accessibility improvements for elderly users
        if (isDropDown) {
            // Larger padding for dropdown items to improve touch targets
            convertView.setPadding(16, 16, 16, 16);
        }
        
        return convertView;
    }
    
    /**
     * Set the color indicator based on the medication color
     * @param colorIndicator View to set color for
     * @param medicationColor Color enum value
     */
    private void setColorIndicator(View colorIndicator, MedicationColor medicationColor) {
        int colorValue;
        
        switch (medicationColor) {
            case WHITE:
                colorValue = Color.WHITE;
                break;
            case YELLOW:
                colorValue = Color.YELLOW;
                break;
            case BLUE:
                colorValue = Color.BLUE;
                break;
            case RED:
                colorValue = Color.RED;
                break;
            case GREEN:
                colorValue = Color.GREEN;
                break;
            case PINK:
                colorValue = Color.parseColor("#FFC0CB"); // Pink
                break;
            case ORANGE:
                colorValue = Color.parseColor("#FFA500"); // Orange
                break;
            case BROWN:
                colorValue = Color.parseColor("#A52A2A"); // Brown
                break;
            case PURPLE:
                colorValue = Color.parseColor("#800080"); // Purple
                break;
            case CLEAR:
                colorValue = Color.TRANSPARENT;
                break;
            case OTHER:
                // Use a gradient or pattern to indicate "other"
                colorValue = Color.GRAY;
                break;
            default:
                colorValue = Color.GRAY;
                break;
        }
        
        // Set background color for the indicator
        if (medicationColor == MedicationColor.CLEAR) {
            // For transparent/clear, show a special pattern
            colorIndicator.setBackgroundResource(R.drawable.color_indicator_background);
            colorIndicator.setAlpha(0.5f);
        } else if (medicationColor == MedicationColor.OTHER) {
            // For "other", show a special pattern or icon
            colorIndicator.setBackgroundColor(colorValue);
            // Add a question mark or pattern overlay for "other"
        } else {
            colorIndicator.setBackgroundColor(colorValue);
            colorIndicator.setAlpha(1.0f);
        }
        
        // Add border for white color to make it visible
        if (medicationColor == MedicationColor.WHITE) {
            colorIndicator.setBackgroundResource(R.drawable.color_indicator_background);
            colorIndicator.setBackgroundColor(Color.WHITE);
        }
    }
    
    /**
     * Get the position of a specific color in the adapter
     * @param color MedicationColor to find
     * @return Position of the color, or -1 if not found
     */
    public int getPosition(MedicationColor color) {
        if (color == null) {
            return -1;
        }
        
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == color) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * ViewHolder pattern for efficient view recycling
     */
    private static class ViewHolder {
        View colorIndicator;
        TextView colorName;
    }
}