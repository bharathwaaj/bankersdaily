package in.bankersdaily.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.bankersdaily.R;
import in.bankersdaily.model.Comment;
import in.bankersdaily.network.ApiClient;
import in.testpress.core.TestpressSdk;
import in.testpress.util.FormatDate;
import in.testpress.util.UILImageGetter;
import in.testpress.util.ViewUtils;
import in.testpress.util.ZoomableImageString;

import static in.bankersdaily.ui.PostDetailFragment.UPDATE_TIME_SPAN;

class CommentsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private List<Comment> comments = new ArrayList<>();

    CommentsListAdapter(Activity activity) {
        this.activity = activity;
    }

    private static class CommentsViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView comment;
        TextView submitDate;
        View divider;

        CommentsViewHolder(View convertView) {
            super(convertView);
            divider = convertView.findViewById(R.id.comment_seperator);
            userName = convertView.findViewById(R.id.user_name);
            comment = convertView.findViewById(R.id.comment);
            submitDate = convertView.findViewById(R.id.submit_date);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.comments_list_item, parent, false);

        return new CommentsViewHolder(v);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof CommentsViewHolder) {
            final CommentsViewHolder holder = (CommentsViewHolder) viewHolder;
            Comment comment = comments.get(position);
            String name = comment.getAuthorName();
            holder.userName.setText(name.substring(0,1).toUpperCase() + name.substring(1));
            Spanned htmlSpan = Html.fromHtml(comment.getContent(),
                    new UILImageGetter(holder.comment, activity), null);

            holder.comment.setText(ZoomableImageString.convertString(htmlSpan, activity, false));
            holder.comment.setMovementMethod(LinkMovementMethod.getInstance());

            updateTimeSpan(comment, holder);

            holder.userName.setTypeface(TestpressSdk.getRubikMediumFont(activity));
            ViewUtils.setTypeface(new TextView[] {holder.submitDate, holder.comment},
                    TestpressSdk.getRubikRegularFont(activity));
        }
    }

    void addPreviousComments(List<Comment> comments) {
        Collections.reverse(comments);
        this.comments.addAll(0, comments);
        notifyItemRangeInserted(0, comments.size());
    }

    void addComments(List<Comment> comments) {
        this.comments.addAll(comments);
        notifyItemRangeInserted(getItemCount(), comments.size());
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position,
                                 List<Object> payloads) {

        if (payloads.isEmpty() || !payloads.get(0).equals(UPDATE_TIME_SPAN)) {
            // Perform a full update
            onBindViewHolder(viewHolder, position);
        } else { // Update time span only
            updateTimeSpan(comments.get(position), (CommentsViewHolder) viewHolder);
        }
    }

    private void updateTimeSpan(Comment comment, CommentsViewHolder holder) {
        //noinspection ConstantConditions
        long submitDateMillis = FormatDate.getDate(comment.getDate(),
                "yyyy-MM-dd'T'HH:mm:ss", ApiClient.TIME_ZONE).getTime();

        holder.submitDate.setText(FormatDate.getAbbreviatedTimeSpan(submitDateMillis));
    }

    List<Comment> getComments() {
        return comments;
    }

}
