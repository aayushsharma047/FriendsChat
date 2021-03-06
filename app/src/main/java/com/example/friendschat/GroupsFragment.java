package com.example.friendschat;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private View group;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> arrayListofgroups= new ArrayList<>();
    private DatabaseReference groupref;


    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        group = inflater.inflate(R.layout.fragment_groups, container, false);
        groupref= FirebaseDatabase.getInstance().getReference().child("Groups");
        initializeFields();
        Retrieveanddisplaygroups();

        //Visit particular Group
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long groupid)
            {
                String currentgroupname= adapterView.getItemAtPosition(position).toString();
                Intent i= new Intent(getContext(), GroupChatActivity.class);
                i.putExtra("groupName", currentgroupname);
                startActivity(i);
            }
        });

        return group;
    }



    private void initializeFields() {
        listView= (ListView) group.findViewById(R.id.grouplistview);
        arrayAdapter= new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arrayListofgroups);
        listView.setAdapter(arrayAdapter);
    }

    private void Retrieveanddisplaygroups() {
        groupref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Set<String> set= new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext())
                {
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }

                arrayListofgroups.clear();
                arrayListofgroups.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

}
