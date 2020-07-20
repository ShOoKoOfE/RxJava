package com.shokoofeadeli.rxjava;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding4.view.RxView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LOG";
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Disposable disposable;
    TextView txtResult;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtResult = findViewById(R.id.txtResult);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // don't send events once the activity is destroyed
        compositeDisposable.clear();
        //don't send events once the activity is destroyed
        disposable.dispose();
    }

    public void createSingleObject(View view) {
        txtResult.setText("");
        final Task task = new Task("Walk the dog");
        Observable<Task> singleTaskObservable = Observable
                .create(new ObservableOnSubscribe<Task>() {
                    @Override
                    public void subscribe(ObservableEmitter<Task> emitter) throws Exception {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(task);
                            emitter.onComplete();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        singleTaskObservable.subscribe(new Observer<Task>() {
            @Override
            public void onSubscribe(Disposable d) { }
            @SuppressLint("SetTextI18n")
            @Override
            public void onNext(Task task) {
                txtResult.setText(txtResult.getText() + "  " + task.getDescription());
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }

    public void createMultiObject(View view) {
        txtResult.setText("");
        final ArrayList<Task> tasks = new ArrayList<>();
        tasks.add(new Task("Take out the trash"));
        tasks.add(new Task("Walk the dog"));
        tasks.add(new Task("Make my bed"));
        tasks.add(new Task("Unload the dishwasher"));
        tasks.add(new Task("Make dinner"));
        Observable<Task> taskListObservable = Observable
                .create(new ObservableOnSubscribe<Task>() {
                    @Override
                    public void subscribe(ObservableEmitter<Task> emitter) throws Exception {
                        for(Task task: tasks){
                            if(!emitter.isDisposed()){
                                emitter.onNext(task);
                            }
                        }
                        if(!emitter.isDisposed()){
                            emitter.onComplete();
                        }
                    }
                })
                .takeWhile(new Predicate<Task>() {
                    @Override
                    public boolean test(Task task) throws Exception {
                        return task.description.equals("Make my bed");
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        taskListObservable.subscribe(new Observer<Task>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @SuppressLint("SetTextI18n")
            @Override
            public void onNext(Task task) {
                txtResult.setText(txtResult.getText() + "  " + task.getDescription());
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }

    public void just(View view) {
        txtResult.setText("");
        //only 10 entries
        Observable.just("first", "second", "third", "fourth", "fifth", "sixth",
                "seventh", "eighth", "ninth", "tenth")
                .take(3)
                .subscribeOn(Schedulers.io()) // What thread to do the work on
                .observeOn(AndroidSchedulers.mainThread()) // What thread to observe the results on
                .subscribe(new Observer<String>() { // view the results by creating a new observer
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe: called");
                    }
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onNext(String s) {
                        txtResult.setText(txtResult.getText() + "  " + s);
                    }
                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: done...");
                    }
                });
    }

    public void range(View view) {
        txtResult.setText("");
        Observable.range(0,11)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) { }
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onNext(Integer integer) {
                        txtResult.setText(txtResult.getText() + "  " + integer);
                    }
                    @Override
                    public void onError(Throwable e) {}
                    @Override
                    public void onComplete() {}
                });
    }

    public void repeat(View view) {
        txtResult.setText("");
        //repeat(n)
        Observable.range(0,3)
                .repeat(2)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {}
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onNext(Integer integer) {
                        txtResult.setText(txtResult.getText() + "  " + integer);
                    }
                    @Override
                    public void onError(Throwable e) {}
                    @Override
                    public void onComplete() {}
                });
    }

    public void interval(View view) {
        txtResult.setText("");
        Observable<Long> intervalObservable = Observable
                .interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .takeWhile(new Predicate<Long>() { // stop the process if more than 5 seconds passes
                    @Override
                    public boolean test(Long aLong) throws Exception {
                        return aLong <= 5;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
        intervalObservable.subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @SuppressLint("SetTextI18n")
            @Override
            public void onNext(Long aLong) {
                txtResult.setText(txtResult.getText() + "  " + aLong);
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }

    public void timer(View view) {
        txtResult.setText("");
        Observable<Long> timeObservable = Observable
                .timer(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        timeObservable.subscribe(new Observer<Long>() {
            long time = 0; // variable for demonstating how much time has passed
            @Override
            public void onSubscribe(Disposable d) {
                time = System.currentTimeMillis() / 1000;
            }
            @SuppressLint("SetTextI18n")
            @Override
            public void onNext(Long aLong) {
                txtResult.setText(txtResult.getText() + "  " + aLong);
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }

    public void fromArray(View view) {
        txtResult.setText("");
        Task[] list = new Task[5];
        list[0] = (new Task("Take out the trash"));
        list[1] = (new Task("Walk the dog"));
        list[2] = (new Task("Make my bed"));
        list[3] = (new Task("Unload the dishwasher"));
        list[4] = (new Task("Make dinner"));
        Observable<Task> taskObservable = Observable
                .fromArray(list)
                .distinct(new Function<Task, String>() {
                    @Override
                    public String apply(Task task) throws Exception {
                        return task.getDescription();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        taskObservable.subscribe(new Observer<Task>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @SuppressLint("SetTextI18n")
            @Override
            public void onNext(Task task) {
                txtResult.setText(txtResult.getText() + "  " + task.getDescription());
            }
            @Override
            public void onError(Throwable e) {}

            @Override
            public void onComplete() {}
        });
    }

    public void fromIrritable(View view) {
        txtResult.setText("");
        List<Task> taskList = new ArrayList<>();
        taskList.add(new Task("Take out the trash"));
        taskList.add(new Task("Walk the dog"));
        taskList.add(new Task("Make my bed"));
        taskList.add(new Task("Unload the dishwasher"));
        taskList.add(new Task("Make dinner"));
        Observable<Task> taskObservable1 = Observable
                .fromIterable(taskList)
                .filter(new Predicate<Task>() {
                    @Override
                    public boolean test(Task task) throws Exception {
                        if(task.getDescription().equals("Walk the dog")){
                            return true;
                        }
                        return false;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        taskObservable1.subscribe(new Observer<Task>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @SuppressLint("SetTextI18n")
            @Override
            public void onNext(Task task) {
                txtResult.setText(txtResult.getText() + "  " + task.getDescription());
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }

    public void fromCallable(View view) {
        txtResult.setText("");
        //Return Task Object From local SQLite database cache
        //Database Process On Background Thread
        //Result Return TO Main Thread
        Observable<Task> callable = Observable
                .fromCallable(new Callable<Task>() {
                    @Override
                    public Task call() throws Exception {
                        Task task1 = new Task("sample");
                        return task1;//MyDatabase.getTask();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        callable.subscribe(new Observer<Task>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @SuppressLint("SetTextI18n")
            @Override
            public void onNext(Task task) {
                txtResult.setText(txtResult.getText() + "  " + task.getDescription());
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }

    public void map(View view) {
        Observable<String> extractDescriptionObservable = Observable
                .fromIterable(DataSource.createTasksList())
                .subscribeOn(Schedulers.io())
                .map(extractDescriptionFunction)
                .observeOn(AndroidSchedulers.mainThread());

        extractDescriptionObservable.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
            }
            @Override
            public void onNext(String s) {
                Log.d(TAG, "onNext: extracted description: " + s);
            }
            @Override
            public void onError(Throwable e) {
            }
            @Override
            public void onComplete() {
            }
        });
    }

    public void buffer(View view) {
        Observable<Task> taskObservable = Observable
                .fromIterable(DataSource.createTasksList())
                .subscribeOn(Schedulers.io());

        taskObservable
                .buffer(2) // Apply the Buffer() operator
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Task>>() { // Subscribe and view the emitted results
                    @Override
                    public void onSubscribe(Disposable d) {
                    }
                    @Override
                    public void onNext(List<Task> tasks) {
                        Log.d(TAG, "onNext: bundle results: -------------------");
                        for(Task task: tasks){
                            Log.d(TAG, "onNext: " + task.getDescription());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                    @Override
                    public void onComplete() {
                    }
                });
    }

    private static class Task {
        private String description;
        public Task(String description) {
            this.description = description;
        }
        public String getDescription() {
            return description;
        }
    }

    Function<Task, String> extractDescriptionFunction = new Function<Task, String>() {
        @Override
        public String apply(Task task) throws Exception {
            Log.d(TAG, "apply: doing work on thread: " + Thread.currentThread().getName());
            return task.getDescription();
        }
    };

    public static class DataSource {

        public static List<Task> createTasksList(){
            List<Task> tasks = new ArrayList<>();
            tasks.add(new Task("Take out the trash"));
            tasks.add(new Task("Walk the dog"));
            tasks.add(new Task("Make my bed"));
            tasks.add(new Task("Unload the dishwasher"));
            tasks.add(new Task("Make dinner"));
            return tasks;
        }

    }

}


//Observable to Flowable:
    //Observable<Integer> observable = Observable.just(1, 2, 3, 4, 5);
    //Flowable<Integer> flowable = observable.toFlowable(BackpressureStrategy.BUFFER);
//Flowable to Observable:
    //Observable<Integer> backToObservable = flowable.toObservable();
