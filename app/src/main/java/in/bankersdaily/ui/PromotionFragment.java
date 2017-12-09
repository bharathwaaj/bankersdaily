package in.bankersdaily.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.bankersdaily.R;
import in.bankersdaily.model.Category;
import in.bankersdaily.model.Post;
import in.bankersdaily.util.ViewUtils;
import in.testpress.core.TestpressSdk;

import static in.bankersdaily.ui.HomePromotionsFragment.DAILY_QUIZ_ID;
import static in.bankersdaily.ui.HomePromotionsFragment.NOTIFICATIONS_ID;
import static in.bankersdaily.ui.PostDetailFragment.POST_SLUG;

public class PromotionFragment extends Fragment {

    @BindView(R.id.promotion_image) ImageView promotionImage;
    @BindView(R.id.title) TextView title;

    Post post;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.promotion_layout, container,
                false);

        ButterKnife.bind(this, rootView);
        post = getArguments().getParcelable("post");

        int emptyImageRes = 0;
        if (post.getFeaturedMedia() == null) {
            if (isCategory(DAILY_QUIZ_ID)) {
                emptyImageRes = R.drawable.quiz_placeholder;
            } else if (isCategory(NOTIFICATIONS_ID)) {
                emptyImageRes = R.drawable.job_notifications_placeholder;
            }
        }
        if (emptyImageRes == 0) {
            emptyImageRes = R.drawable.login_screen_image;
        }
        Glide.with(getActivity())
                .load(post.getFeaturedMedia())
                .placeholder(R.drawable.placeholder_icon)
                .error(emptyImageRes)
                .into(promotionImage);

        promotionImage.setMaxHeight(ViewUtils.getImageHeightWithRespectToDevice(getActivity()));
        title.setText(Html.fromHtml(post.getTitle()));
        title.setTypeface(TestpressSdk.getRubikMediumFont(getActivity()));
        return rootView;
    }

    boolean isCategory(int categoryId) {
        for (Category category : post.getCategories()) {
            if (category.getId() == categoryId) {
                return true;
            }
        }
        return false;
    }

    @OnClick(R.id.card_view) void onClick() {
        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
        intent.putExtra(POST_SLUG, post.getSlug());
        getActivity().startActivity(intent);
    }
}
