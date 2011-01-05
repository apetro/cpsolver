package net.sf.cpsolver.studentsct.weights;

import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Request;

/**
 * Interface to model various student weightings
 * 
 * @version StudentSct 1.2 (Student Sectioning)<br>
 *          Copyright (C) 2007 - 2010 Tomas Muller<br>
 *          <a href="mailto:muller@unitime.org">muller@unitime.org</a><br>
 *          <a href="http://muller.unitime.org">http://muller.unitime.org</a><br>
 * <br>
 *          This library is free software; you can redistribute it and/or modify
 *          it under the terms of the GNU Lesser General Public License as
 *          published by the Free Software Foundation; either version 3 of the
 *          License, or (at your option) any later version. <br>
 * <br>
 *          This library is distributed in the hope that it will be useful, but
 *          WITHOUT ANY WARRANTY; without even the implied warranty of
 *          MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *          Lesser General Public License for more details. <br>
 * <br>
 *          You should have received a copy of the GNU Lesser General Public
 *          License along with this library; if not see
 *          <a href='http://www.gnu.org/licenses/'>http://www.gnu.org/licenses/</a>.
 */

public interface StudentWeights {
    /**
     * Return lower bound for the given request
     * @param request given request
     * @return weight of the best value
     */
    public double getBound(Request request);
    
    /**
     * Return weight of the given enrollment 
     * @param enrollment given enrollment
     * @param nrDistanceConflicts number of distance conflicts
     * @param timeOverlappingConflicts number of time overlapping conflicts
     * @return weight (higher weight means better value)
     */
    public double getWeight(Enrollment enrollment, int nrDistanceConflicts, int timeOverlappingConflicts);
}