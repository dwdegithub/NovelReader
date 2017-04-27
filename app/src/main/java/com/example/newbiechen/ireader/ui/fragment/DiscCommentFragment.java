package com.example.newbiechen.ireader.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;

import com.example.newbiechen.ireader.R;
import com.example.newbiechen.ireader.RxBus;
import com.example.newbiechen.ireader.event.SelectorEvent;
import com.example.newbiechen.ireader.model.bean.BookCommentBean;
import com.example.newbiechen.ireader.model.flag.BookDistillate;
import com.example.newbiechen.ireader.model.flag.BookSort;
import com.example.newbiechen.ireader.model.flag.CommunityType;
import com.example.newbiechen.ireader.presenter.DiscCommentPresenter;
import com.example.newbiechen.ireader.presenter.contract.DiscCommentContact;
import com.example.newbiechen.ireader.ui.adapter.DiscCommentAdapter;
import com.example.newbiechen.ireader.ui.base.BasePresenter;
import com.example.newbiechen.ireader.ui.base.BaseRxFragment;
import com.example.newbiechen.ireader.utils.Constant;
import com.example.newbiechen.ireader.widget.itemdecoration.DashItemDecoration;
import com.example.newbiechen.ireader.widget.refresh.ScrollRefreshRecyclerView;
import com.example.newbiechen.ireader.widget.adapter.WholeAdapter;

import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * DiscussionCommentFragment
 * Created by newbiechen on 17-4-17.
 * 讨论组中的评论区，包括Comment、Girl、Origin
 * 1. 初始化RecyclerView
 * 2. 初始化视图和逻辑的交互
 */

public class DiscCommentFragment extends BaseRxFragment implements DiscCommentContact.View{
    private static final String TAG = "DiscCommentFragment";
    private static final String EXTRA_BLOCK = "extra_block";
    private static final String BUNDLE_BLOCK = "bundle_block";
    private static final String BUNDLE_SORT = "bundle_sort";
    private static final String BUNDLE_DISTILLATE = "bundle_distillate";
    private static final String BUNDLE_START = "bundle_start";
    /***********************view********************************/
    @BindView(R.id.discussion_rv_content)
    ScrollRefreshRecyclerView mRvContent;

    /************************object**********************************/
    private DiscCommentAdapter mDiscCommentAdapter;
    private DiscCommentContact.Presenter mPresenter;

    /*************************Params*******************************/
    private String mBlock = CommunityType.COMMENT.getTypeName();
    private String mBookSort = BookSort.DEFAULT.getNetName();
    private String mDistillate = BookDistillate.ALL.getNetName();
    private int mStart = 0;
    private final int mLimited = 20;

    /****************************open method**********************************/
    public static Fragment newInstance(String block){
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_BLOCK,block);
        Fragment fragment = new DiscCommentFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    /**********************************************************************/

    @Override
    protected int getContentId() {
        return R.layout.fragment_discussion;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mBlock = getArguments().getString(EXTRA_BLOCK);
        if (savedInstanceState != null){
            mBlock = savedInstanceState.getString(BUNDLE_BLOCK);
            mBookSort = savedInstanceState.getString(BUNDLE_SORT);
            mDistillate = savedInstanceState.getString(BUNDLE_DISTILLATE);
            mStart = savedInstanceState.getInt(BUNDLE_START);
        }
    }

    @Override
    protected void initWidget(Bundle savedInstanceState) {
        setUpAdapter();
    }

    private void setUpAdapter(){
        mRvContent.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvContent.addItemDecoration(new DashItemDecoration());
        mDiscCommentAdapter = new DiscCommentAdapter(getContext(),new WholeAdapter.Options());
        mRvContent.setAdapter(mDiscCommentAdapter);
    }

    @Override
    protected BasePresenter createPresenter() {
        return new DiscCommentPresenter(this);
    }

    /******************************init click method***********************************/

    @Override
    protected void initClick() {
        //下滑刷新
        mRvContent.setOnRefreshListener( () -> refreshData() );
        //上滑加载
        mDiscCommentAdapter.setOnLoadMoreListener(
                () -> mPresenter.loadingDiscussion(mBlock, mBookSort, mStart, mLimited, mDistillate)
        );

        //选择刷新
        RxBus.getInstance()
                .toObservable(Constant.MSG_SELECTOR, SelectorEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (event) ->{
                            mBookSort = event.sort.getNetName();
                            mDistillate = event.distillate.getNetName();
                            refreshData();
                        },null,null,
                        (disposable) ->{
                            //添加到Disposable中
                            addDisposable(disposable);
                        }
                );
    }

    /*******************************logic********************************/
    @Override
    protected void processLogic() {
        super.processLogic();
        //刷新数据
        refreshData();
    }

    private void refreshData(){
        mStart = 0;
        mPresenter.refreshDiscussion(mBlock, mBookSort, mStart, mLimited, mDistillate);

    }
    /********************************rewrite method****************************************/
    @Override
    public void setPresenter(DiscCommentContact.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showRefreshView() {
        mRvContent.startRefresh();//开启刷新动画
    }

    @Override
    public void finishRefreshView() {
        mRvContent.setRefreshing(false);
    }

    @Override
    public void finishRefresh(List<BookCommentBean> discussionBeans){
        mDiscCommentAdapter.refreshItems(discussionBeans);
        mStart = discussionBeans.size();
    }

    @Override
    public void finishLoading(List<BookCommentBean> discussionBeans) {
        mDiscCommentAdapter.addItems(discussionBeans);
        mStart += discussionBeans.size();
    }

    @Override
    public void loadError() {
        //错误的点击事件默认为重新加载，所以不需要设置监听
        mDiscCommentAdapter.showLoadError();
    }

    /****************************save*************************************/
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_BLOCK, mBlock);
        outState.putString(BUNDLE_SORT,mBookSort);
        outState.putString(BUNDLE_DISTILLATE,mDistillate);
        outState.putInt(BUNDLE_START,mStart);
    }
}