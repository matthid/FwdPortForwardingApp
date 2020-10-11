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

package com.elixsr.portforwarder.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import com.elixsr.portforwarder.db.RuleContract;
import com.elixsr.portforwarder.db.RuleDbHelper;
import com.elixsr.portforwarder.models.RuleModel;
import com.elixsr.portforwarder.util.RuleHelper;

import static com.facebook.GraphRequest.TAG;

/**
 * The {@link RuleDao} class provides common functionality for Rule database access.
 * <p>
 * This class provides common database access functions.
 *
 * @author Niall McShane
 * @see <a href="http://developer.android.com/training/basics/data-storage/databases.html#ReadDbRow"></a>
 */
public class RuleDao {

    private RuleDbHelper ruleDbHelper;

    public RuleDao(RuleDbHelper ruleDbHelper) {
        this.ruleDbHelper = ruleDbHelper;
    }

    /**
     * Inserts a valid rule into the SQLite database.
     *
     * @param ruleModel The source {@link RuleModel}.
     * @return the id of the inserted rule.
     */
    public long insertRule(RuleModel ruleModel) {
        // Gets the data repository in write mode
        try(SQLiteDatabase db = ruleDbHelper.getWritableDatabase())
        {
            ContentValues constantValues = RuleHelper.ruleModelToContentValues(ruleModel);

            long newRowId = db.insert(
                    RuleContract.RuleEntry.TABLE_NAME,
                    null,
                    constantValues);

            ruleModel.setId(newRowId);
            Log.i(TAG, "Rule #" + newRowId + "(" + ruleModel.getName() + ") inserted to database. "
                    + ruleModel.getFromInterfaceName() + "(" + ruleModel.getFromPortMin() + "-" + ruleModel.getFromPortMax() + " >> "
                    + ruleModel.getTargetIp() + "(" + ruleModel.getTargetPortMin() + "-max) ..." + constantValues.toString());

            return newRowId;
        }
    }

    public void updateRule(RuleModel ruleModel) {
        try (SQLiteDatabase db = ruleDbHelper.getReadableDatabase()) {

            // New model to store
            ContentValues values = RuleHelper.ruleModelToContentValues(ruleModel);

            // Which row to update, based on the ID
            String selection = RuleContract.RuleEntry.COLUMN_NAME_RULE_ID + "=?";
            String[] selectionArgs = {String.valueOf(ruleModel.getId())};
            int count = db.update(
                    RuleContract.RuleEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);

            Log.i(TAG, "Rule #" + ruleModel.getId() + "(" + ruleModel.getName() + ") updated in the database. "
                    + ruleModel.getFromInterfaceName() + "(" + ruleModel.getFromPortMin() + "-" + ruleModel.getFromPortMax() + " >> "
                    + ruleModel.getTargetIp() + "(" + ruleModel.getTargetPortMin() + "-max) ..." + values.toString());

            // Close db
            db.close();
        }

    }

    /**
     * Finds and returns a list of all rules.
     *
     * @return a list of all {@link RuleModel} objects.
     */
    public List<RuleModel> getAllRuleModels() {

        List<RuleModel> ruleModels = new LinkedList<RuleModel>();

        // Gets the data repository in read mode
        try (SQLiteDatabase db = ruleDbHelper.getReadableDatabase()) {

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            String[] projection = RuleDbHelper.generateAllRowsSelection();

            // How you want the results sorted in the resulting Cursor
            String sortOrder =
                    RuleContract.RuleEntry.COLUMN_NAME_RULE_ID + " DESC";

            Cursor cursor = db.query(
                    RuleContract.RuleEntry.TABLE_NAME,          // The table to query
                    projection,                                 // The columns to return
                    null,                                       // The columns for the WHERE clause
                    null,                                       // The values for the WHERE clause
                    null,                                       // don't group the rows
                    null,                                       // don't filter by row groups
                    sortOrder                                   // The sort order
            );

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                RuleModel ruleModel = RuleHelper.cursorToRuleModel(cursor);
                ruleModels.add(ruleModel);

                Log.i(TAG, "Rule #" + ruleModel.getId() + "(" + ruleModel.getName() + ") read from database. "
                        + ruleModel.getFromInterfaceName() + "(" + ruleModel.getFromPortMin() + "-" + ruleModel.getFromPortMax() + " >> "
                        + ruleModel.getTargetIp() + "(" + ruleModel.getTargetPortMin() + "-max) ...");

                cursor.moveToNext();
            }
            // make sure to close the cursor
            cursor.close();

            return ruleModels;
        }
    }

    public List<RuleModel> getAllEnabledRuleModels() {
        List<RuleModel> ruleModels = new LinkedList<RuleModel>();
        List<RuleModel> enabledRuleModels = new LinkedList<RuleModel>();

        ruleModels = getAllRuleModels();

        for (RuleModel ruleModel : ruleModels) {
            if (ruleModel.isEnabled()) {
                enabledRuleModels.add(ruleModel);
            }
        }

        return enabledRuleModels;
    }

    public RuleModel getRule(long ruleModelId) {

        try (SQLiteDatabase db = ruleDbHelper.getReadableDatabase()) {

            Cursor cursor = db.query(
                    RuleContract.RuleEntry.TABLE_NAME,
                    RuleDbHelper.generateAllRowsSelection(),
                    RuleContract.RuleEntry.COLUMN_NAME_RULE_ID + "=?",
                    new String[]{String.valueOf(ruleModelId)},
                    null,
                    null,
                    null
            );

            cursor.moveToFirst();

            RuleModel ruleModel = RuleHelper.cursorToRuleModel(cursor);

            Log.i(TAG, "Rule #" + ruleModel.getId() + "(" + ruleModel.getName() + ") read from database. "
                    + ruleModel.getFromInterfaceName() + "(" + ruleModel.getFromPortMin() + "-" + ruleModel.getFromPortMax() + " >> "
                    + ruleModel.getTargetIp() + "(" + ruleModel.getTargetPortMin() + "-max) ...");

            // Close the DB
            cursor.close();
            //db.close();

            return ruleModel;
        }

    }

    public void deleteRule(long ruleModelId) {
        try (SQLiteDatabase db = ruleDbHelper.getReadableDatabase()) {

            // Define 'where' part of query.
            String selection = RuleContract.RuleEntry.COLUMN_NAME_RULE_ID + "=?";
            // Specify arguments in placeholder order.
            String[] selectionArgs = {String.valueOf(ruleModelId)};
            // Issue SQL statement.
            db.delete(RuleContract.RuleEntry.TABLE_NAME, selection, selectionArgs);

            Log.i(TAG, "Rule #" + ruleModelId + " deleted from database...");
        }
    }
}
