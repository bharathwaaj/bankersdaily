package in.bankersdaily.ui;

import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import in.bankersdaily.R;
import in.bankersdaily.model.Category;
import in.bankersdaily.model.Post;
import in.bankersdaily.util.FormatDate;
import in.bankersdaily.util.SingleTypeAdapter;
import in.bankersdaily.util.ViewUtils;
import in.testpress.core.TestpressSdk;

import static in.bankersdaily.ui.PostDetailFragment.POST_SLUG;

public class PostSearchListAdapter extends SingleTypeAdapter<Post> {

    private Activity activity;
    private boolean hideCategoryLabel;

    PostSearchListAdapter(Activity activity) {
        super(activity, R.layout.post_search_list_item);
        this.activity = activity;
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.title, R.id.date, R.id.category_layout, R.id.category,
                R.id.post_item_layout, R.id.image_view };
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = super.getView(position, null, parent);
            ViewUtils.setTypeface(new TextView[] { textView(0), textView(1), textView(3) },
                    TestpressSdk.getRubikRegularFont(activity));

            return convertView;
        }
        return super.getView(position, convertView, parent);
    }

    @Override
    protected void update(final int position, final Post post) {
        Glide.with(activity)
                .load(post.getFeaturedMediaSquare())
                .placeholder(R.drawable.placeholder_icon)
                .error(R.mipmap.ic_launcher)
                .into(imageView(5));

        setText(0, Html.fromHtml(post.getTitle()));
        setText(1, FormatDate.getAbbreviatedTimeSpan(post.getDate().getTime()));
        if (post.getCategories().isEmpty() || hideCategoryLabel) {
            setGone(2, true);
        } else {
            setGone(2, false);
            StringBuilder categoryString = new StringBuilder();
            List<Category> categories = post.getCategories();
            for (int i = 0; i < categories.size();) {
                categoryString.append(categories.get(i).getName());
                if (++i < categories.size()) {
                    categoryString.append(", ");
                }
            }
            setText(3, categoryString);
        }
        view(4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, PostDetailActivity.class);
                intent.putExtra(POST_SLUG, post.getSlug());
                activity.startActivity(intent);
            }
        });
    }

    public void setHideCategoryLabel(boolean hideCategoryLabel) {
        this.hideCategoryLabel = hideCategoryLabel;
    }
}
