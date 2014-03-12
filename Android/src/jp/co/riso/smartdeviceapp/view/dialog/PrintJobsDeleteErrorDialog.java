package jp.co.riso.smartdeviceapp.view.dialog;

import jp.co.riso.smartdeviceapp.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class PrintJobsDeleteErrorDialog extends DialogFragment implements OnClickListener {
    
       public PrintJobsDeleteErrorDialog() {
        super();
    }
    
    public static PrintJobsDeleteErrorDialog newInstance() {
        PrintJobsDeleteErrorDialog dialog = null;
        
        dialog = new PrintJobsDeleteErrorDialog();
        return dialog;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog dialog;
        
        LayoutInflater inflater = LayoutInflater.from(context);
        View deleteView = inflater.inflate(R.layout.dialog_deletejobs, null);
        
        TextView title = (TextView) deleteView.findViewById(R.id.deleteJobsTitle);
        TextView message = (TextView) deleteView.findViewById(R.id.deleteJobsMessage);
        
        title.setText("Delete Printer Jobs");
        message.setText("Deletion failed.");
        
        builder.setView(deleteView);
        builder.setPositiveButton(R.string.ids_lbl_ok, this);

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        
        return dialog;
    }
    
    public void onClick(DialogInterface dialog, int which) {
        
        dialog.dismiss();
    }
    
}