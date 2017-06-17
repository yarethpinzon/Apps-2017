package com.example.arturrinkis.universitystudentrating;


import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.arturrinkis.universitystudentrating.CacheData.ProfileCache;
import com.example.arturrinkis.universitystudentrating.CustomControls.Adapters.DisciplineAdapter;
import com.example.arturrinkis.universitystudentrating.DTO.Discipline;
import com.example.arturrinkis.universitystudentrating.ServerServiceAPI.Interfaces.IServerAPIManager;
import com.example.arturrinkis.universitystudentrating.ServerServiceAPI.ServerAPIManager;

import java.util.ArrayList;


public class ProfessorDisciplinesFragment extends Fragment {
    private String PROFESSOR_DISCIPLINES_FRAGMENT_TAG = "PROFESSOR_DISCIPLINES_FRAGMENT_TAG";
    private View view = null;
    private IServerAPIManager serverAPIManager = null;
    private GetDisciplineByProfessorAsyncTask getDisciplineByProfessorAsyncTask = null;

    public ProfessorDisciplinesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        view = inflater.inflate(R.layout.fragment_statistic_disciplines, container, false);
        serverAPIManager = new ServerAPIManager(getActivity().getApplicationContext());

        initMainHeaderTextView();
        hideMainContent();
        showLoadingAnimation();


        if(savedInstanceState == null || ProfileCache.getInstance().getDisciplines() == null) {
            getDisciplineByProfessorAsyncTask = new GetDisciplineByProfessorAsyncTask();
            getDisciplineByProfessorAsyncTask.execute(ProfileCache.getInstance().getAuthUser().getUserProfileId());
        }
        else {
            initControls();
        }

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cancelGetDisciplineByBranchAsyncTask();
    }

    private void initControls() {
        try {
            initDisciplinesListView();
            hideLoadingAnimation();
            showMainContent();
        } catch (NullPointerException e) {
            Log.e("asynch task", "Activity has been destroyed");
        }
    }

    private void initMainHeaderTextView(){
        TextView main_header_tv = (TextView)getActivity().findViewById(R.id.main_header_tv);
        main_header_tv.setText("Select subject");
    }

    private void initDisciplinesListView(){
        final DisciplineAdapter disciplineAdapter = new DisciplineAdapter(view.getContext(), ProfileCache.getInstance().getDisciplines());

        ListView disciplines_lv = (ListView) view.findViewById(R.id.disciplines_lv);
        disciplines_lv.setAdapter(disciplineAdapter);

        disciplines_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Discipline discipline = (Discipline)disciplineAdapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putInt("DisciplineId", discipline.getId());
                bundle.putString("DisciplineName", discipline.getName());
                ProfessorRatingFragment professorRatingFragment = new ProfessorRatingFragment();
                professorRatingFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.tab_container, professorRatingFragment);
                fragmentTransaction.addToBackStack(PROFESSOR_DISCIPLINES_FRAGMENT_TAG);
                fragmentTransaction.commit();
            }
        });
    }

    private void showLoadingAnimation() {
        RelativeLayout loading_layout = (RelativeLayout) getActivity().findViewById(R.id.loading_layout);
        loading_layout.setVisibility(View.VISIBLE);
    }

    private void hideMainContent() {
        ListView disciplines_lv = (ListView) view.findViewById(R.id.disciplines_lv);
        disciplines_lv.setVisibility(View.GONE);
    }

    private void hideLoadingAnimation() {
        RelativeLayout loading_layout = (RelativeLayout) getActivity().findViewById(R.id.loading_layout);
        loading_layout.setVisibility(View.GONE);
    }

    private void showMainContent(){
        ListView disciplines_lv = (ListView) view.findViewById(R.id.disciplines_lv);
        disciplines_lv.setVisibility(View.VISIBLE);
    }

    //
    //
    //database async tasks

    class GetDisciplineByProfessorAsyncTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            ArrayList<Discipline> disciplines = serverAPIManager.getDataServiceAPI().getDisciplinesByProfessor(params[0].intValue());
            ProfileCache.getInstance().setDisciplines(disciplines);

            return null;
        }
        protected void onPostExecute(Void result){
            initControls();
        }
    }

    private void cancelGetDisciplineByBranchAsyncTask(){
        if (getDisciplineByProfessorAsyncTask == null) return;
        getDisciplineByProfessorAsyncTask.cancel(false);
    }
}