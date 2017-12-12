package fr.kanopytest.maximedonnet.kanopytest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Custom adapter to display repositories objects on the associated layout ListView.
 * @extends ArrayAdapter<Repository>
 */
public class RepositoryAdapter extends ArrayAdapter<Repository> {
    public RepositoryAdapter(Context context, List<Repository> repositories) {
        super(context, 0, repositories);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.repositories_list_row, parent, false);
        }

        RepositoryAdapter.RepositoryViewHolder viewHolder = (RepositoryAdapter.RepositoryViewHolder) convertView.getTag();
        if(viewHolder == null){
            viewHolder = new RepositoryAdapter.RepositoryViewHolder();
            viewHolder.full_name = convertView.findViewById(R.id.repositoryFullName);

            convertView.setTag(viewHolder);
        }

        Repository repository = getItem(position);

        viewHolder.full_name.setText(repository.getFullName());

        return convertView;
    }

    private class RepositoryViewHolder{
        public TextView full_name;
    }
}
