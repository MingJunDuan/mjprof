/*
       This file is part of mjprof.

        mjprof is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        mjprof is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with mjprof.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.performizeit.mjprof.plugins.mappers;

import com.performizeit.mjprof.plugin.types.DumpReducer;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.model.ThreadInfoAggregator;
import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.plumbing.PipeHandler;

import java.util.ArrayList;


@Plugin(name="group", params ={@Param(type = String.class,value = "attr",optional=true,defaultValue = "")},description="Group a single thread dump by an attribute. If not attribute is specified all dump is merged")
public class GroupByProp implements DumpReducer,PipeHandler<ThreadDump,ThreadDump>  {
    private final String prop;
    public GroupByProp(String prop) {
        this.prop = prop;
    }
    public ThreadDump map(final ThreadDump jsd ) {
        ArrayList<String> a= new ArrayList<String>();
        a.add(prop);
        ThreadInfoAggregator aggr = new ThreadInfoAggregator(a);
        for (ThreadInfo mss : jsd.getStacks()  ) {
            aggr.accumulateThreadInfo(mss);
        }
        ThreadDump jsd2 = new ThreadDump();
        jsd2.setHeader(jsd.getHeader());
        jsd2.setStacks(aggr.getAggrInfos());
        jsd2.setJNIglobalReferences(jsd.getJNIglobalReferences());
        return jsd2;
    }

    @Override public ThreadDump handleMsg(ThreadDump msg) { return map(msg);}
    @Override public ThreadDump handleDone() {return null;}

}
