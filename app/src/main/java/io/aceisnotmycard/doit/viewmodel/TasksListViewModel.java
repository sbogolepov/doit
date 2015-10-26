package io.aceisnotmycard.doit.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import java.util.List;

import io.aceisnotmycard.doit.db.TaskDao;
import io.aceisnotmycard.doit.model.Task;
import io.aceisnotmycard.doit.pipeline.Pipe;
import io.aceisnotmycard.doit.pipeline.events.TaskRemovedEvent;
import io.aceisnotmycard.doit.pipeline.events.TaskUpdatedEvent;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class TasksListViewModel extends BaseViewModel {

    private ObservableArrayList<Task> items;
    private Context context;

    public TasksListViewModel(Context context) {
        super();
        this.context = context;
        items = new ObservableArrayList<>();
        addSubscription(TaskDao.getDao(context).getTasks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tasks -> {
                    items.clear();
                    for (Task task : tasks) {
                        items.add(task);
                    }
            }));

        addSubscription(Pipe.getObservable()
                .filter(abstactEvent -> abstactEvent instanceof TaskRemovedEvent)
                .map(abstactEvent1 -> (TaskRemovedEvent) abstactEvent1)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(taskRemovedEvent -> TaskDao.getDao(context).delete(taskRemovedEvent.getData())
                ));

        addSubscription(Pipe.getObservable()
                .filter(abstactEvent -> abstactEvent instanceof TaskUpdatedEvent)
                .map(abstactEvent1 -> (TaskUpdatedEvent) abstactEvent1)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(taskUpdatedEvent -> TaskDao.getDao(context).update(taskUpdatedEvent.getData())
            ));
    }

    public ObservableArrayList<Task> getItems() {
        return items;
    }

}

