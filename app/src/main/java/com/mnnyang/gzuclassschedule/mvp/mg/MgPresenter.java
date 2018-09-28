package com.mnnyang.gzuclassschedule.mvp.mg;

import android.text.TextUtils;

import com.mnnyang.gzuclassschedule.R;
import com.mnnyang.gzuclassschedule.app.app;
import com.mnnyang.gzuclassschedule.data.bean.CsItem;
import com.mnnyang.gzuclassschedule.data.db.CourseDbDao;
import com.mnnyang.gzuclassschedule.utils.DialogHelper;
import com.mnnyang.gzuclassschedule.utils.Preferences;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by mnnyang on 17-11-4.
 */

public class MgPresenter implements MgContract.Presenter {
    MgContract.View mView;
    MgContract.Model mModel;
    ArrayList<CsItem> mCsItems;

    public MgPresenter(MgContract.View view, ArrayList<CsItem> csItems) {
        mView = view;
        mView.setPresenter(this);

        mCsItems = csItems;
        mModel = new MgModel();
    }

    @Override
    public void start() {
        reloadCsNameList();
    }

    @Override
    public void reloadCsNameList() {

        Observable.create(new ObservableOnSubscribe<ArrayList<CsItem>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<CsItem>> emitter) throws Exception {
                ArrayList<CsItem> data = mModel.getCsItemData();
                emitter.onNext(data);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<CsItem>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ArrayList<CsItem> csItems) {
                        mCsItems.clear();
                        mCsItems.addAll(csItems);
                        mView.showList(mCsItems);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        /*Observable.create(new Observable.OnSubscribe<ArrayList<CsItem>>() {
            @Override
            public void call(Subscriber<? super ArrayList<CsItem>> subscriber) {
                ArrayList<CsItem> data = mModel.getCsItemData();
                subscriber.onNext(data);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<CsItem>>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ArrayList<CsItem> items) {
                        mCsItems.clear();
                        mCsItems.addAll(items);
                        mView.showList(mCsItems);
                    }
                });*/
    }

    @Override
    public void addCsName(String csName) {
        if (TextUtils.isEmpty(csName)) {
            mView.showNotice(app.mContext.getString(R.string.course_name_can_not_be_empty));
        } else {
            //TODO 检查
            boolean isConflict = CourseDbDao.instance().hasConflictCourseTableName(csName);
            if (isConflict) {
                //notice conflict
                mView.showNotice(app.mContext.getString(R.string.course_name_is_conflicting));
            } else {
                //add cs_name
                CourseDbDao.instance().getCsNameId(csName);
                mView.addCsNameSucceed();
            }
        }
    }

    @Override
    public void editCsName(int id, String newCsName) {

        int update = CourseDbDao.instance().updateCsName(id, newCsName);
        if (update == 0) {
            mView.showNotice(app.mContext.getString(R.string.course_name_already_exists));
        } else {
            mView.editCsNameSucceed();
        }
    }

    @Override
    public void deleteCsName(final int csNameId, final DialogHelper dh) {


        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                CourseDbDao dao = CourseDbDao.instance();
                dao.removeByCsNameId(csNameId);
                emitter.onNext("ok");
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        dh.hideProgressDialog();
                        mView.deleteFinish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        dh.hideProgressDialog();
                    }

                    @Override
                    public void onComplete() {

                    }
                });

        /*Observable.create(new Observable.OnSubscribe<String>() {

            @Override
            public void call(Subscriber<? super String> subscriber) {
                CourseDbDao dao = CourseDbDao.instance();
                dao.removeByCsNameId(csNameId);
                subscriber.onNext("ok");
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        dh.hideProgressDialog();
                    }

                    @Override
                    public void onNext(String s) {
                        dh.hideProgressDialog();
                        mView.deleteFinish();
                    }
                });*/
    }

    @Override
    public void switchCsName(int csNameId) {
        Preferences.putInt(app.mContext.getString(
                R.string.app_preference_current_cs_name_id), csNameId);

        mView.showNotice("切换成功");
        mView.gotoCourseActivity();
    }
}
