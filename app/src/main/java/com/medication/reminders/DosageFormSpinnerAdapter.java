package com.medication.reminders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Custom spinner adapter for medication dosage form selection
 * Displays dosage form options with icons and localized names
 * Designed for elderly users with clear, readable text and accessibility considerations
 */
public class DosageFormSpinnerAdapter extends BaseAdapter {
    
    private final Context context;
    private final MedicationDosageForm[] dosageForms;
    private final LayoutInflater inflater;
    
    /**
     * Constructor for DosageFormSpinnerAdapter
     * @param context Android context
     */
    public DosageFormSpinnerAdapter(Context context) {
        this.context = context;
        this.dosageForms = MedicationDosageForm.getAllDosageForms();
        this.inflater = LayoutInflater.from(context);
    }
    
    @Override
    public int getCount() {
        return dosageForms.length;
    }
    
    @Override
    public MedicationDosageForm getItem(int position) {
        return dosageForms[position];
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
            convertView = inflater.inflate(R.layout.spinner_dosage_form_item, parent, false);
            holder = new ViewHolder();
            holder.dosageFormIcon = convertView.findViewById(R.id.dosageFormIcon);
            holder.dosageFormName = convertView.findViewById(R.id.dosageFormName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        MedicationDosageForm dosageForm = dosageForms[position];
        
        // Set dosage form name with localized text
        holder.dosageFormName.setText(dosageForm.getDisplayName(context));
        
        // Set dosage form icon
        setDosageFormIcon(holder.dosageFormIcon, dosageForm);
        
        // Apply accessibility improvements for elderly users
        if (isDropDown) {
            // Larger padding for dropdown items to improve touch targets
            convertView.setPadding(16, 16, 16, 16);
            
            // Ensure text is large enough for elderly users
            holder.dosageFormName.setTextSize(18);
        }
        
        return convertView;
    }
    
    /**
     * Set the icon based on the medication dosage form
     * @param iconView TextView to set icon for
     * @param dosageForm Dosage form enum value
     */
    private void setDosageFormIcon(TextView iconView, MedicationDosageForm dosageForm) {
        String icon;
        
        switch (dosageForm) {
            case PILL:
                icon = "💊"; // Pill emoji
                break;
            case TABLET:
                icon = "⚪"; // White circle for tablet
                break;
            case CAPSULE:
                icon = "💊"; // Pill emoji (similar to pill)
                break;
            case LIQUID:
                icon = "🧪"; // Test tube for liquid
                break;
            case INJECTION:
                icon = "💉"; // Syringe emoji
                break;
            case POWDER:
                icon = "⚪"; // White circle for powder
                break;
            case CREAM:
                icon = "🧴"; // Bottle emoji for cream
                break;
            case PATCH:
                icon = "🏷️"; // Label emoji for patch
                break;
            case INHALER:
                icon = "🫁"; // Lungs emoji for inhaler (fallback to generic if not supported)
                break;
            case OTHER:
                icon = "❓"; // Question mark for other
                break;
            default:
                icon = "💊"; // Default pill emoji
                break;
        }
        
        iconView.setText(icon);
        
        // Special handling for "OTHER" option
        if (dosageForm == MedicationDosageForm.OTHER) {
            // Make the "other" option visually distinct
            iconView.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        } else {
            // Reset to default color for other options
            iconView.setTextColor(context.getResources().getColor(android.R.color.black));
        }
    }
    
    /**
     * Get the position of a specific dosage form in the adapter
     * @param dosageForm MedicationDosageForm to find
     * @return Position of the dosage form, or -1 if not found
     */
    public int getPosition(MedicationDosageForm dosageForm) {
        if (dosageForm == null) {
            return -1;
        }
        
        for (int i = 0; i < dosageForms.length; i++) {
            if (dosageForms[i] == dosageForm) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * ViewHolder pattern for efficient view recycling
     */
    private static class ViewHolder {
        TextView dosageFormIcon;
        TextView dosageFormName;
    }
}