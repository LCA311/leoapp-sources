package de.slg.messenger;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import de.slg.essensqr.WrapperQRActivity;
import de.slg.klausurplan.KlausurplanActivity;
import de.slg.leoapp.PreferenceActivity;
import de.slg.leoapp.R;
import de.slg.leoapp.User;
import de.slg.leoapp.Utils;
import de.slg.schwarzes_brett.SchwarzesBrettActivity;
import de.slg.startseite.MainActivity;
import de.slg.stimmungsbarometer.StimmungsbarometerActivity;
import de.slg.stundenplan.WrapperStundenplanActivity;

public class OverviewWrapper extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    public ChatsFragment cFragment;
    public UserFragment uFragment;
    public Chat[] chatArray = null;
    public User[] userArray = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.registerOverviewWrapper(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrapper_messenger);

        initToolbar();
        initArrays();
        initNavigationView();
        initTabs();

        Utils.getNotificationManager().cancelAll();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.messenger_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        } else if (item.getItemId() == R.id.action_add) {
            if (Utils.checkNetwork()) {
                startActivity(new Intent(getApplicationContext(), AddGroupChatActivity.class));
            } else {
                Toast.makeText(getApplicationContext(), "Verbinde dich mit dem Internet um neue Gruppen zu erstellen.", Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    public void finish() {
        Utils.registerOverviewWrapper(null);
        super.finish();
    }

    private void initToolbar() {
        Toolbar actionBar = (Toolbar) findViewById(R.id.actionBarOverview);
        actionBar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
        actionBar.setTitle("Messenger");
        setSupportActionBar(actionBar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initTabs() {
        cFragment = new ChatsFragment();
        uFragment = new UserFragment();

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                if (position == 1)
                    return uFragment;
                return cFragment;
            }

            @Override
            public int getCount() {
                return 2;
            }
        };

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_question_answer_white_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_person_white_24dp);
    }

    private void initNavigationView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.getMenu().findItem(R.id.messenger).setChecked(true);

        navigationView.getMenu().findItem(R.id.nachhilfe).setEnabled(Utils.isVerified());
        navigationView.getMenu().findItem(R.id.messenger).setEnabled(Utils.isVerified());
        navigationView.getMenu().findItem(R.id.klausurplan).setEnabled(Utils.isVerified());
        navigationView.getMenu().findItem(R.id.stundenplan).setEnabled(Utils.isVerified());

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawers();
                Intent i;
                switch (menuItem.getItemId()) {
                    case R.id.foodmarks:
                        i = new Intent(getApplicationContext(), WrapperQRActivity.class);
                        break;
                    case R.id.messenger:
                        return true;
                    case R.id.newsboard:
                        i = new Intent(getApplicationContext(), SchwarzesBrettActivity.class);
                        break;
                    case R.id.nachhilfe:
                        i = new Intent(getApplicationContext(), MainActivity.class);
                        break;
                    case R.id.stundenplan:
                        i = new Intent(getApplicationContext(), WrapperStundenplanActivity.class);
                        break;
                    case R.id.barometer:
                        i = new Intent(getApplicationContext(), StimmungsbarometerActivity.class);
                        break;
                    case R.id.klausurplan:
                        i = new Intent(getApplicationContext(), KlausurplanActivity.class);
                        break;
                    case R.id.startseite:
                        i = null;
                        break;
                    case R.id.settings:
                        i = new Intent(getApplicationContext(), PreferenceActivity.class);
                        break;
                    default:
                        i = new Intent(getApplicationContext(), MainActivity.class);
                        Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
                if (i != null)
                    startActivity(i);
                finish();
                return true;
            }
        });
        TextView username = (TextView) navigationView.getHeaderView(0).findViewById(R.id.username);
        username.setText(Utils.getUserName());
        ImageView mood = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.profile_image);
        mood.setImageResource(Utils.getCurrentMoodRessource());
    }

    private void initArrays() {
        userArray = Utils.getMessengerDBConnection().getUsers();
        chatArray = Utils.getMessengerDBConnection().getChats();
        Utils.receive();
    }

    public int indexOf(Chat c) {
        if (c != null && chatArray != null)
            for (int i = 0; i < chatArray.length; i++)
                if (c.equals(chatArray[i]))
                    return i;
        return -1;
    }

    public User findUser(int id) {
        for (User u : userArray)
            if (u.uid == id)
                return u;
        return null;
    }

    public void notifyUpdate() {
        chatArray = Utils.getMessengerDBConnection().getChats();
        userArray = Utils.getMessengerDBConnection().getUsers();
        uFragment.refreshUI();
        cFragment.refreshUI();
        ChatActivity chatActivity = Utils.getChatActivity();
        if (chatActivity != null)
            chatActivity.refreshUI(true, false);
    }

    public static class UserFragment extends Fragment {
        public View rootView;
        public ListView lvUsers;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_user_overview, container, false);

            initListView();

            return rootView;
        }

        private void initListView() {
            lvUsers = (ListView) rootView.findViewById(R.id.listViewUser);
            lvUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position < Utils.getOverviewWrapper().userArray.length) {
                        User clickedUser = Utils.getOverviewWrapper().userArray[position];
                        Chat newChat = new Chat(-1, "" + clickedUser.uid + " - " + Utils.getCurrentUser().uid, Chat.Chattype.PRIVATE);
                        int index = Utils.getOverviewWrapper().indexOf(newChat);
                        if (index == -1) {
                            new CreateChat().execute(newChat);
                            ChatActivity.currentChat = newChat;
                        } else {
                            ChatActivity.currentChat = Utils.getOverviewWrapper().chatArray[index];
                        }
                        startActivity(new Intent(getContext(), ChatActivity.class));
                    }
                }
            });
            lvUsers.setAdapter(new UserAdapter(getContext(), Utils.getOverviewWrapper().userArray, false));
        }

        public void refreshUI() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lvUsers.setAdapter(new UserAdapter(getContext(), Utils.getOverviewWrapper().userArray, false));
                }
            });
        }
    }

    public static class ChatsFragment extends Fragment {
        public View rootView;
        public ListView lvChats;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_chat_overview, container, false);

            initListView();

            return rootView;
        }

        private void initListView() {
            lvChats = (ListView) rootView.findViewById(R.id.listViewChats);
            lvChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position < Utils.getOverviewWrapper().chatArray.length) {
                        ChatActivity.currentChat = Utils.getOverviewWrapper().chatArray[position];
                        startActivity(new Intent(getContext(), ChatActivity.class));
                    }
                }
            });
            lvChats.setAdapter(new ChatAdapter(Utils.getOverviewWrapper().getApplicationContext(), Utils.getOverviewWrapper().chatArray));
        }

        public void refreshUI() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lvChats.setAdapter(new ChatAdapter(getContext(), Utils.getOverviewWrapper().chatArray));
                }
            });
        }
    }

    private static class ChatAdapter extends ArrayAdapter<Chat> {
        private LayoutInflater inflater;
        private int resId;
        private Chat[] chats;

        ChatAdapter(Context context, Chat[] chats) {
            super(context, R.layout.list_item_chat, chats);
            this.inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            this.resId = R.layout.list_item_chat;
            this.chats = chats;
        }

        @NonNull
        @Override
        public View getView(int position, View v, @NonNull ViewGroup parent) {
            if (v == null) {
                v = inflater.inflate(resId, null);
            }
            TextView chatname = (TextView) v.findViewById(R.id.chatname);
            TextView lastSender = (TextView) v.findViewById(R.id.letzteNachrichtAbsender);
            TextView lastMessage = (TextView) v.findViewById(R.id.letzteNachrichtText);
            ImageView icon = (ImageView) v.findViewById(R.id.iconChat);
            if (position < chats.length && chats[position] != null) {
                chatname.setText(chats[position].cname);
                if (chats[position].m != null) {
                    lastMessage.setText(chats[position].m.mtext);
                    lastSender.setText(chats[position].m.uname);
                    if (!chats[position].m.mread)
                        v.findViewById(R.id.notify).setVisibility(View.VISIBLE);
                    else
                        v.findViewById(R.id.notify).setVisibility(View.GONE);
                } else {
                    v.findViewById(R.id.textView3).setVisibility(View.INVISIBLE);
                    v.findViewById(R.id.notify).setVisibility(View.INVISIBLE);
                }
                if (chats[position].ctype == Chat.Chattype.PRIVATE) {
                    icon.setImageResource(R.drawable.ic_chat_bubble_white_24dp);
                } else {
                    icon.setImageResource(R.drawable.ic_question_answer_white_24dp);
                }
            }
            return v;
        }
    }

    private static class CreateChat extends AsyncTask<Chat, Void, Void> {
        @Override
        protected Void doInBackground(Chat... params) {
            if (Utils.checkNetwork()) {
                try {
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            new URL(generateURL(params[0]))
                                                    .openConnection()
                                                    .getInputStream(), "UTF-8"));
                    String erg = "";
                    String l;
                    while ((l = reader.readLine()) != null)
                        erg += l;
                    if (!erg.startsWith("error"))
                        params[0].cid = Integer.parseInt(erg);
                    else
                        Log.e("Error", erg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        private String generateURL(Chat chat) {
            String chatname = chat.cname.replace(' ', '+');
            return "http://moritz.liegmanns.de/messenger/addChat.php?key=5453&chatname=" + chatname + "&chattype=" + Chat.Chattype.PRIVATE.toString().toLowerCase();
        }
    }
}