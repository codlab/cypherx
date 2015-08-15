package eu.codlab.cypherx.tuto;

import android.app.Activity;
import android.view.View;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.ArrayList;
import java.util.List;

import eu.codlab.cypherx.ApplicationController;
import eu.codlab.cypherx.R;

/**
 * Created by kevinleperf on 11/08/15.
 */
public class TutoHelper {
    private ShowcaseView _last_view = null;
    private String _last_key = null;
    private List<ShowcaseView.Builder> _builders = new ArrayList<>();


    public void openTuto(Activity activity, final String key, final int title, int text,
                         View target) {
        openTuto(activity, key, title, text, 0, target);
    }

    public void openTuto(Activity activity, final String key, final int title, int text,
                         int id, View target) {
        ViewTarget view_target = null;
        if (id != 0)
            view_target = new ViewTarget(id, target);
        else
            view_target = new ViewTarget(target);

        if (!ApplicationController.getInstance().getBoolean(key, false)) {
            ShowcaseView.Builder builder = new ShowcaseView.Builder(activity)
                    .setContentText(text)
                    .setContentTitle(title)
                    .setStyle(R.style.ShowcaseViewCypher)
                    .setShowcaseEventListener(new OnShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                            ApplicationController.getInstance().setBoolean(key, true);
                            pop();
                            showNext();
                        }

                        @Override
                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                        }

                        @Override
                        public void onShowcaseViewShow(ShowcaseView showcaseView) {

                        }
                    })
                    .setTarget(view_target);
            if (_last_key != null && _last_key.equals(key)) {
                clean();
                pop();
            }
            _builders.add(builder);
            _last_key = key;
            if (_builders.size() == 1) {
                showNext();
            }
        }
    }

    private void pop() {
        if (_builders.size() > 0) _builders.remove(0);
    }

    private void showNext() {
        if (_builders.size() > 0) {
            _last_view = _builders.get(0).build();
            _last_view.show();
        }
    }

    public void clean() {
        if (_last_view != null) {
            _last_view.hide();
        }
    }
}
