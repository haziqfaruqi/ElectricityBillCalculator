package com.example.electricitybillcalculator;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Bill.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract BillDao billDao();
}