/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.ivy.plugins.conflict;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.cache.CacheManager;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ConfigurationResolveReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.IvyNode;
import org.apache.ivy.core.resolve.ResolveOptions;

public class LatestConflictManagerTest extends TestCase {

	private Ivy ivy;

	protected void setUp() throws Exception {
		ivy = new Ivy();
		ivy.configure(LatestConflictManagerTest.class
				.getResource("ivyconf-latest.xml"));
	}

	// Test case for issue IVY-388
	public void testIvy388() throws Exception {
		ResolveReport report = ivy.resolve(LatestConflictManagerTest.class
				.getResource("ivy-388.xml"), 
				getResolveOptions());

		List deps = report.getDependencies();
		Iterator dependencies = deps.iterator();
		String[] confs = report.getConfigurations();
		while (dependencies.hasNext()) {
			IvyNode node = (IvyNode) dependencies.next();
			for (int i = 0; i < confs.length; i++) {
				String conf = confs[i];
				if (!node.isEvicted(conf)) {
					boolean flag1 = report.getConfigurationReport(conf)
							.getDependency(node.getResolvedId()) != null;
					boolean flag2 = report.getConfigurationReport(conf)
							.getModuleRevisionIds().contains(node.getResolvedId());
					assertEquals("Inconsistent data for node " + node + " in conf " + conf , flag1, flag2);
				}
			}
		}
	}
	
    // Test case for issue IVY-383
    public void testIvy383() throws Exception {
        ResolveReport report =
            ivy.resolve( LatestConflictManagerTest.class.getResource( "ivy-383.xml" ), 
            		getResolveOptions() );
        ConfigurationResolveReport defaultReport =
            report.getConfigurationReport("default");
        Iterator iter = defaultReport.getModuleRevisionIds().iterator();
        while (iter.hasNext()) {
            ModuleRevisionId mrid = (ModuleRevisionId)iter.next();
            if (mrid.getName().equals("mod1.1")) {
                assertEquals("1.0", mrid.getRevision());
            }
            else if (mrid.getName().equals("mod1.2")) {
                assertEquals("2.2", mrid.getRevision());
            }
        }
    }
    
    // Test case for issue IVY-407
    public void testLatestTime1() throws Exception {
		ivy = new Ivy();
		ivy.configure(LatestConflictManagerTest.class
				.getResource("ivyconf-latest-time.xml"));
        ResolveReport report =
            ivy.resolve( LatestConflictManagerTest.class.getResource( "ivy-latest-time-1.xml" ), 
            		getResolveOptions() );
        ConfigurationResolveReport defaultReport =
            report.getConfigurationReport("default");
        Iterator iter = defaultReport.getModuleRevisionIds().iterator();
        while (iter.hasNext()) {
            ModuleRevisionId mrid = (ModuleRevisionId)iter.next();
            if (mrid.getName().equals("mod1.1")) {
                assertEquals("1.0", mrid.getRevision());
            }
            else if (mrid.getName().equals("mod1.2")) {
                assertEquals("2.2", mrid.getRevision());
            }
        }
    }
    
    public void testLatestTime2() throws Exception {
		ivy = new Ivy();
		ivy.configure(LatestConflictManagerTest.class
				.getResource("ivyconf-latest-time.xml"));
        ResolveReport report =
            ivy.resolve( LatestConflictManagerTest.class.getResource( "ivy-latest-time-2.xml" ), 
            		getResolveOptions() );
        ConfigurationResolveReport defaultReport =
            report.getConfigurationReport("default");
        Iterator iter = defaultReport.getModuleRevisionIds().iterator();
        while (iter.hasNext()) {
            ModuleRevisionId mrid = (ModuleRevisionId)iter.next();
            if (mrid.getName().equals("mod1.1")) {
                assertEquals("1.0", mrid.getRevision());
            }
            else if (mrid.getName().equals("mod1.2")) {
                assertEquals("2.2", mrid.getRevision());
            }
        }
    }
    
    /*
    Test case for issue IVY-407 (with transitivity)

    There are 5 modules A, B, C, D and E.
		1) publish C-1.0.0, C-1.0.1 and C-1.0.2
		2) B needs C-1.0.0 : retrieve ok and publish B-1.0.0
		3) A needs B-1.0.0 and C-1.0.2 : retrieve ok and publish A-1.0.0
		4) D needs C-1.0.1 : retrieve ok and publish D-1.0.0
		5) E needs D-1.0.0 and A-1.0.0 (D before A in ivy file) retrieve failed to get C-1.0.2 from A
		(get apparently C-1.0.1 from D)
     */
    public void testLatestTimeTransitivity() throws Exception {
    	ivy = new Ivy();
    	ivy.configure(LatestConflictManagerTest.class
    			.getResource("ivyconf-latest-time-transitivity.xml"));
    	ivy.getSettings().setVariable("ivy.log.conflict.resolution", "true", true);
    	ResolveReport report =
    		ivy.resolve( LatestConflictManagerTest.class.getResource( "ivy-latest-time-transitivity.xml" ), 
    				getResolveOptions() );
    	ConfigurationResolveReport defaultReport =
    		report.getConfigurationReport("default");
    	Iterator iter = defaultReport.getModuleRevisionIds().iterator();
    	while (iter.hasNext()) {
    		ModuleRevisionId mrid = (ModuleRevisionId)iter.next();

    		if (mrid.getName().equals("A")) {
    			assertEquals("A revision should be 1.0.0", "1.0.0", mrid.getRevision());
    		}
    		else if (mrid.getName().equals("D")) {
    			assertEquals("D revision should be 1.0.0", "1.0.0", mrid.getRevision());
    		}
    		// by transitivity
    		else if (mrid.getName().equals("B")) {
    			assertEquals("B revision should be 1.0.0", "1.0.0", mrid.getRevision());
    		}
    		else if (mrid.getName().equals("C")) {
    			assertEquals("C revision should be 1.0.2", "1.0.2", mrid.getRevision());
    		}
    	}
    }

    private ResolveOptions getResolveOptions() {
		return new ResolveOptions()
			.setCache(CacheManager.getInstance(ivy.getSettings()))
			.setValidate(false);
	}
}
