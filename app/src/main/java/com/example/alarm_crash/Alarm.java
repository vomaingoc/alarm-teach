/**************************************************************************
 *
 * Copyright (C) 2012-2015 Alex Taradov <alex@taradov.com>
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
 *
 *************************************************************************/

package com.example.alarm_crash;

import java.lang.System;
import java.lang.Comparable;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Calendar;

import android.content.Intent;
import android.content.Context;

public class Alarm implements Comparable<Alarm>
{
  private Context context;
  private long id;
  private String title;
  private long date;
  private boolean enabled;
  private boolean vibrate;
  private int days;
  private long nextOccurence;

  public static final int NEVER = 0;
  public static final int EVERY_DAY = 0x7f;

  public Alarm(Context context)
  {
    this.context = context;
    id = 0;
    title = "";
    date = System.currentTimeMillis();
    enabled = true;
    vibrate = true;
    days = EVERY_DAY;
    update();
  }

  public long getId()
  {
    return id;
  }

  public void setId(long id)
  {
    this.id = id;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public long getDate()
  {
    return date;
  }

  public void setDate(long date)
  {
    this.date = date;
    update();
  }

  public boolean getEnabled()
  {
    return enabled;
  }

  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
  }

  public boolean getVibrate() { return vibrate; }

  public void setVibrate(boolean vibrate)
  {
    this.vibrate = vibrate;
  }

  public int getDays()
  {
    return days;
  }

  public void setDays(int days)
  {
    this.days = days;
    update();
  }

  public long getNextOccurence()
  {
    return nextOccurence;
  }

  public boolean getOutdated()
  {
    return nextOccurence < System.currentTimeMillis();
  }

  public int compareTo(Alarm aThat)
  {
    final long thisNext = getNextOccurence();
    final long thatNext = aThat.getNextOccurence();
    final int BEFORE = -1;
    final int EQUAL = 0;
    final int AFTER = 1;

    if (this == aThat)
      return EQUAL;

    if (thisNext > thatNext)
      return AFTER;
    else if (thisNext < thatNext)
      return BEFORE;
    else
      return EQUAL;
  }

  public void update()
  {
    Calendar now = Calendar.getInstance();

    Calendar alarm = Calendar.getInstance();

    alarm.setTimeInMillis(date);
    alarm.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

    if (days != NEVER)
    {
      while (true)
      {
        int day = (alarm.get(Calendar.DAY_OF_WEEK) + 5) % 7;

        if (alarm.getTimeInMillis() > now.getTimeInMillis() && (days & (1 << day)) > 0)
          break;

        alarm.add(Calendar.DAY_OF_MONTH, 1);
      }
    }
    else
    {
      alarm.add(Calendar.YEAR, 10);
    }

    nextOccurence = alarm.getTimeInMillis();

    date = nextOccurence;
  }

  public void toIntent(Intent intent)
  {
    intent.putExtra("com.example.alarm_crash.id", id);
    intent.putExtra("com.example.alarm_crash.title", title);
    intent.putExtra("com.example.alarm_crash.date", date);
    intent.putExtra("com.example.alarm_crash.alarm", enabled);
    intent.putExtra("com.example.alarm_crash.vibrate", vibrate);
    intent.putExtra("com.example.alarm_crash.days", days);
  }

  public void fromIntent(Intent intent)
  {
    id = intent.getLongExtra("com.example.alarm_crash.id", 0);
    title = intent.getStringExtra("com.example.alarm_crash.title");
    date = intent.getLongExtra("com.example.alarm_crash.date", 0);
    enabled = intent.getBooleanExtra("com.example.alarm_crash.alarm", true);
    vibrate = intent.getBooleanExtra("com.example.alarm_crash.vibrate", true);
    days = intent.getIntExtra("com.example.alarm_crash.days", 0);
    update();
  }

  public void serialize(DataOutputStream dos) throws IOException
  {
    dos.writeLong(id);
    dos.writeUTF(title);
    dos.writeLong(date);
    dos.writeBoolean(enabled);
    dos.writeBoolean(vibrate);
    dos.writeInt(days);
  }
 
  public void deserialize(DataInputStream dis) throws IOException
  {
    id = dis.readLong();
    title = dis.readUTF();
    date = dis.readLong();
    enabled= dis.readBoolean();
    vibrate = dis.readBoolean();
    days = dis.readInt();
    update();
  }
}