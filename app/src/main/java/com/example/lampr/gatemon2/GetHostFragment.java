package com.example.lampr.gatemon2;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class GetHostFragment extends Fragment {

    private static final String TAG = "GetHostFRAGMENT";
    // Create a new instance of DetailsFragment, initialized to show the
    // text at 'index'.

    public static GetHostFragment newInstance(int index) {
        GetHostFragment f = new GetHostFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);

        return f;
    }


    public GetHostFragment() {
        // Required empty public constructor
    }



    // DetailsFragment LifeCycle
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach()");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, " onDestroyView()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, " onDetach()");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_get_host, container, false);
    }



}



//    public int getShownIndex() {
//        return getArguments().getInt("index", 0);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        Toast.makeText(getActivity(), "DetailsFragment:onCreateView",
//                Toast.LENGTH_LONG).show();
//
//        // programmatically create a scrollview and texview for the text in
//        // the container/fragment layout. Set up the properties and add the
//        // view.
//
//        ScrollView scroller = new ScrollView(getActivity());
//        TextView text = new TextView(getActivity());
//        int padding = (int) TypedValue.applyDimension(
//                TypedValue.COMPLEX_UNIT_DIP, 4, getActivity()
//                        .getResources().getDisplayMetrics());
//        text.setPadding(padding, padding, padding, padding);
//        scroller.addView(text);
//        text.setText(Shakespeare.DIALOGUE[getShownIndex()]);
//        return scroller;
//    }
