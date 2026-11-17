package com.davidrdz.vitallog.ui.agenda;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.ui.common.BaseScreenActivity;

/** Pantalla "Mis medicamentos y citas". En el Hito 3 recibe las listas y los formularios. */
public class AgendaActivity extends BaseScreenActivity {

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_agenda;
    }

    @Override
    protected int getScreenTitleRes() {
        return R.string.title_agenda;
    }
}
