package jp.co.riso.smartdeviceapp.view.dialog;

import jp.co.riso.smartdeviceapp.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class PrintJobsDeleteDialog extends DialogFragment implements OnClickListener {
    
    private static final String TAG = "PrintJobsDeleteDialog";
    private static final String KEY_PRINTERID = "delete_jobs";
    private int printerId;
    private PrintJobsDeleteDialogListener listener;
        
    public PrintJobsDeleteDialog() {
        super();
    }
    
    public static PrintJobsDeleteDialog newInstance(int pid) {
        PrintJobsDeleteDialog dialog = null;
        
        dialog = new PrintJobsDeleteDialog();
        Bundle extras = new Bundle();
        extras.putInt(KEY_PRINTERID, pid);
        dialog.setArguments(extras);
        return dialog;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            printerId = getArguments().getInt(KEY_PRINTERID);
        }
        
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog dialog;
        
        LayoutInflater inflater = LayoutInflater.from(context);
        View deleteView = inflater.inflate(R.layout.dialog_deletejobs, null);
        
        TextView title = (TextView) deleteView.findViewById(R.id.deleteJobsTitle);
        TextView message = (TextView) deleteView.findViewById(R.id.deleteJobsMessage);
        
        title.setText("Delete Printer Jobs");
        message.setText("Are you sure you want to delete these print jobs?");
        
        builder.setView(deleteView);
        builder.setPositiveButton(R.string.ids_lbl_ok, this);
        builder.setNegativeButton(R.string.ids_lbl_cancel, this);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        
        return dialog;
    }
    
    public void onClick(DialogInterface dialog, int which) {
        
        Log.d("CESTEST", TAG + (listener!=null?"not null":"null"));
        if (listener != null) {
            
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    listener.onDelete(printerId);
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    //dismiss
                    break;
                default:
                    Log.d(TAG, "action not supported");
                    break;
            }
        }
        
        dialog.dismiss();
    }
    
    public void setListener(PrintJobsDeleteDialogListener l){
        this.listener = l;
    }
    
    public interface PrintJobsDeleteDialogListener {
        public void onDelete(int printerId);
    }
}