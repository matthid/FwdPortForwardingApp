/*
 * Fwd: the port forwarding app
 * Copyright (C) 2016  Elixsr Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.elixsr.portforwarder.ui.preferences;

import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.elixsr.portforwarder.R;
import com.elixsr.portforwarder.ui.BaseActivity;
import com.elixsr.portforwarder.ui.BaseWebActivity;

/**
 * Created by Niall McShane on 08/03/2016.
 */
public class HelpActivity extends BaseWebActivity {

    private static final String URL = "http://support.elix.sr/#!/products/fwd";

    public HelpActivity() {
        super(URL);
    }
}
