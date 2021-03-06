package com.udacity.backingapp.ui.activity;

import android.databinding.DataBindingUtil;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.udacity.backingapp.R;
import com.udacity.backingapp.api.RecipeApi;
import com.udacity.backingapp.api.RetrofitCall;
import com.udacity.backingapp.databinding.MainRecipesListBinding;
import com.udacity.backingapp.model.Recipe;
import com.udacity.backingapp.test.mIdingResource;
import com.udacity.backingapp.ui.fragment.RecipeFragment;
import com.udacity.backingapp.utils.ConnectionUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainRecipesList extends AppCompatActivity {
    @Nullable
    private mIdingResource idingResource;

    public static final String RECIPES = "recipes";
    private RecipeApi service;
    private List<Recipe> recipeList;
    private MainRecipesListBinding mMainBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainBinding = DataBindingUtil.setContentView(this, R.layout.main_recipes_list);

        getIdlingResource();

        if (idingResource != null) {
            idingResource.setIdleState(false);
        }



        recipeList = new ArrayList<>();
        getRecipes();

    }


    void getRecipes() {
        if (ConnectionUtils.checkConnection(this)) {
            service = RetrofitCall.getRecipes().create(RecipeApi.class);
            Observable<List<Recipe>> observable = service.getRecipes();
            observable.observeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<Recipe>>() {
                        @Override
                        public void call(List<Recipe> recipes) {
                            recipeList = recipes;
                            if (idingResource != null) {
                                idingResource.setIdleState(false);
                            }
                            launchRecipeFragment(recipes);
                        }

                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.internet_connection), Toast.LENGTH_SHORT).show();
        }
    }

    void launchRecipeFragment(List<Recipe> recipes) {

        RecipeFragment recipeFragment = new RecipeFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(RECIPES, (ArrayList<? extends Parcelable>) recipes);
        recipeFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .add(mMainBinding.mainContainer.getId(), recipeFragment)
                .commit();

    }

    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (idingResource == null) {
            idingResource = new mIdingResource();
        }
        return idingResource;
    }
}
