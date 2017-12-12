package fr.kanopytest.maximedonnet.kanopytest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Custom adapter to display commits objects on the associated layout ListView.
 * @extends ArrayAdapter<Commit>
 */
public class CommitAdapter extends ArrayAdapter<Commit> {

    public CommitAdapter(Context context, List<Commit> commits) {
        super(context, 0, commits);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.commits_list_row, parent, false);
        }

        CommitViewHolder viewHolder = (CommitViewHolder) convertView.getTag();
        if(viewHolder == null){
            viewHolder = new CommitViewHolder();
            viewHolder.title = convertView.findViewById(R.id.commitTitle);
            viewHolder.committer = convertView.findViewById(R.id.commitCommitter);
            viewHolder.date = convertView.findViewById(R.id.commitDate);

            convertView.setTag(viewHolder);
        }

        Commit commit = getItem(position);

        viewHolder.title.setText("Title : " + commit.getTitle());
        viewHolder.date.setText("Date : " + commit.getDate());
        viewHolder.committer.setText("Committer : " + commit.getCommitter());

        return convertView;
    }

    private class CommitViewHolder{
        public TextView title;
        public TextView committer;
        public TextView date;
    }
}