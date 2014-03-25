/*
       This file is part of mjstack.

        mjstack is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        mjstack is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.performizeit.mjstack.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by life on 23/2/14.
 */
public class JStackMetadataStack {
    HashMap<String, Object> metaData = new HashMap<String, Object>();


    public JStackMetadataStack(String stackTrace) {
        BufferedReader reader = new BufferedReader(new StringReader(stackTrace));
        try {
            String metaLine = reader.readLine();
            if (metaLine != null) {
                parseMetaLine(metaLine);

                String threadState = reader.readLine();
                if (threadState != null) {
                    parseThreadState(threadState);
                }
                String linesOfStack = "";
                String s;
                while ((s = reader.readLine()) != null) {
                    if (s.trim().length() == 0) break;
                    linesOfStack += s + "\n";
                }
                metaData.put("stack", new JStackStack(linesOfStack));


                while ((s = reader.readLine()) != null) {
                   if (s.contains("Locked ownable synchronizers"))     break;

                }

                String linesOfLOS = "";
                while ((s = reader.readLine()) != null) {
                    if (s.trim().length() == 0) break;
                    linesOfLOS += s + "\n";
                }
                if (linesOfLOS.trim().length() > 0)
                    metaData.put("los", new JstackLockedOwbnableSynchronizers(linesOfLOS));

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println(this.toString());
  //      System.out.println(stackTrace);


    }
    public JStackMetadataStack(HashMap <String,Object> mtd) {
        metaData =mtd;
    }
    public HashMap<String, Object> cloneMetaData() {
        return (HashMap<String, Object>) metaData.clone();
    }

    private void parseThreadState(String threadState) {
        Pattern p = Pattern.compile("^[\\s]*java.lang.Thread.State: (.*)$");
        Matcher m = p.matcher(threadState);
        if (m.find()) {
            metaData.put("state", m.group(1));
        }
    }


    public Object getVal(String key) {
        return metaData.get(key);
    }

    protected String metadataProperty(String metaLine,String propertyName){
        Pattern p = Pattern.compile(".* "+propertyName+"=([0-9a-fx]*) .*");
        Matcher m = p.matcher(metaLine);

        if (m.find()) {
            return  m.group(1);

        }
        return null;
    }
    private void parseMetaLine(String metaLine) {
        Pattern p = Pattern.compile("^\"(.*)\".*");
        Matcher m = p.matcher(metaLine);

        if (m.find()) {
            metaData.put("name", m.group(1));
        }

        extractStatus(metaLine);
        String prio = metadataProperty(metaLine,"prio");
        if (prio != null)
            metaData.put("prio", Integer.parseInt(prio));
        String tid = metadataProperty(metaLine,"tid");
        if (tid != null) {
            metaData.put("tid", new HexaLong(tid));
            metaData.put("tidstr", tid);
        }
        String nid = metadataProperty(metaLine,"nid");
        if (nid != null) {
            metaData.put("nid", new HexaLong(nid));
            metaData.put("nidstr", nid);
        }
        if (metaLine.contains("\" daemon ")) {
            metaData.put("daemon", true);
        }

    }

    private void extractStatus(String metaLine) {
        int idx = metaLine.lastIndexOf('=');
        if (idx != -1) {
            String lastParam = metaLine.substring(idx);
            idx =         lastParam.indexOf(' ') ;
            if (idx != -1 ) {

                lastParam = lastParam.substring(idx+1);

                if (lastParam.length() > 0) {
                    metaData.put("status", lastParam.trim());
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder mdStr = new StringBuilder();
        if (metaData.get("name") != null) {
            mdStr.append("\"" + metaData.get("name") + "\"");
        }
        if (metaData.get("daemon") != null) {
            mdStr.append(" daemon");
        }
        if (metaData.get("prio") != null) {
            mdStr.append(" prio=" + metaData.get("prio"));
        }
        if (metaData.get("tidstr") != null) {
            mdStr.append(" tid=" + metaData.get("tidstr"));
        }
        if (metaData.get("nidstr") != null) {
            mdStr.append(" nid=" + metaData.get("nidstr"));
        }
        if (metaData.get("status") != null) {
            mdStr.append(" " + metaData.get("status"));
        }


        if (metaData.get("state") != null) {
            mdStr.append("\n   java.lang.Thread.State: ").append( metaData.get("state"));
        }
        if (metaData.get("stack") != null) {
            mdStr.append("\n").append(metaData.get("stack").toString()).append("\n");
        }
        if (metaData.get("los") != null) {
            mdStr.append("   Locked ownable synchronizers:\n").append(
            metaData.get("los").toString());
        }
        return mdStr.toString();

    }

    public Set<String> getProps() {
        return metaData.keySet();
    }
}
