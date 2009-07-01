/**
 * Copyright 2009 University of Oxford
 *
 * Written by Arno Mittelbach for the Erewhon Project
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the University of Oxford nor the names of its 
 *    contributors may be used to endorse or promote products derived from this 
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.oucs.gaboto.model.query.defined;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.oucs.gaboto.entities.pool.GabotoEntityPool;
import org.oucs.gaboto.exceptions.CorruptDataException;
import org.oucs.gaboto.exceptions.GabotoException;
import org.oucs.gaboto.exceptions.GabotoRuntimeException;
import org.oucs.gaboto.exceptions.NoTimeIndexSetException;
import org.oucs.gaboto.model.Gaboto;
import org.oucs.gaboto.model.GabotoSnapshot;
import org.oucs.gaboto.model.query.GabotoQueryImpl;
import org.oucs.gaboto.timedim.TimeInstant;

/**
 * Query that allows to execute a SPARQL Construct query.
 * 
 * @author Arno Mittelbach
 * @version 0.1
 */
public class SimpleConstructSPARQLQuery extends GabotoQueryImpl {

  private TimeInstant timeInstant;
  private String query;

  public SimpleConstructSPARQLQuery(TimeInstant ti, File query) throws GabotoException {
    super();
    this.timeInstant = ti;

    readFile(query);
  }

  public SimpleConstructSPARQLQuery(TimeInstant ti, String query) throws GabotoException {
    super();
    this.timeInstant = ti;
    this.query = query;
  }

  public SimpleConstructSPARQLQuery(Gaboto gaboto, TimeInstant ti, File query) {
    super(gaboto);
    this.timeInstant = ti;

    readFile(query);
  }

  public SimpleConstructSPARQLQuery(Gaboto gaboto, TimeInstant ti, String query) {
    super(gaboto);
    this.timeInstant = ti;
    this.query = query;
  }

  private void readFile(File queryFile) {
    if (!queryFile.exists())
      throw new IllegalArgumentException(queryFile.getAbsolutePath() + " does not exist");

    StringBuilder contents = new StringBuilder();
    try {
      BufferedReader input = new BufferedReader(new FileReader(queryFile));
      try {
        String line = null;
        while ((line = input.readLine()) != null) {
          contents.append(line);
          contents.append(System.getProperty("line.separator"));
        }
      } catch (IOException e) {
        throw new GabotoRuntimeException(e);
      } finally {
        input.close();
      }
    } catch (IOException e) {
      throw new GabotoRuntimeException(e);
    }
    this.query = contents.toString();
  }

  @Override
  public int getResultType() {
    return GabotoQueryImpl.RESULT_TYPE_ENTITY_POOL;
  }

  @Override
  protected Object execute() throws NoTimeIndexSetException, CorruptDataException {
    GabotoSnapshot snapshot = getGaboto().getSnapshot(timeInstant);

    GabotoSnapshot intermediateSnap = snapshot.execSPARQLConstruct(query);

    // create resultPool from model
    GabotoEntityPool resultPool = intermediateSnap.buildEntityPool();

    // set snapshot
    resultPool.setSnapshot(snapshot);

    return resultPool;
  }

  @Override
  protected void doPrepare() throws GabotoException {
    // TODO Auto-generated method stub

  }

}
