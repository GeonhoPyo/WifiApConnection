package mureung.wifiapconnection.WifiConnect;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import mureung.wifiapconnection.R;

public class WifiListAdapter  extends ArrayAdapter<String> {

    private ArrayList<String> wifiArrayList = null;
    private static ViewHolder holder;

    public WifiListAdapter(@NonNull Context context, int resource, ArrayList<String> wifiArrayList) {
        super(context, resource);
        this.wifiArrayList = new ArrayList<String>();
        this.wifiArrayList = wifiArrayList;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = ( LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.list_wificonnect , null);


        return convertView;
    }

    private static class ViewHolder{
        private TextView wifiConnectListText;
    }
    private static void initViewItem(View view){
        holder = new ViewHolder();
        holder.wifiConnectListText = (TextView)view.findViewById(R.id.wifiConnectListText);
    }
}
