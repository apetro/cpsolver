package org.cpsolver.ifs.assignment.context;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.BinaryConstraint;
import org.cpsolver.ifs.model.Model;
import org.cpsolver.ifs.model.Value;
import org.cpsolver.ifs.model.Variable;

/**
 * A binary constraint with an assignment context. This is a variant of the {@link ConstraintWithContext} that extends the
 * {@link BinaryConstraint} class.
 * 
 * 
 * @see AssignmentContext
 * 
 * @version IFS 1.2 (Iterative Forward Search)<br>
 *          Copyright (C) 2014 Tomas Muller<br>
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
 *          License along with this library; if not see <http://www.gnu.org/licenses/>.
 **/
public abstract class BinaryConstraintWithContext<V extends Variable<V, T>, T extends Value<V, T>, C extends AssignmentConstraintContext<V, T>> extends BinaryConstraint<V, T> implements HasAssignmentContext<V, T, C>, CanHoldContext {
    private AssignmentContextReference<V, T, C> iContextReference = null;
    private AssignmentContext[] iContext = null;
    
    public BinaryConstraintWithContext() {
        super();
    }
    
    @Override
    public void setModel(Model<V, T> model) {
        super.setModel(model);
        if (model != null)
            iContextReference = model.createReference(this);
    }
    
    /**
     * Returns an assignment context associated with this constraint. If there is no 
     * assignment context associated with this constraint yet, one is created using the
     * {@link ConstraintWithContext#createAssignmentContext(Assignment)} method. From that time on,
     * this context is kept with the assignment and automatically updated by calling the
     * {@link AssignmentConstraintContext#assigned(Assignment, Value)} and {@link AssignmentConstraintContext#unassigned(Assignment, Value)}
     * whenever a variable of this constraint is changed.
     * @param assignment given assignment
     * @return assignment context associated with this constraint and the given assignment
     */
    @SuppressWarnings("unchecked")
    @Override
    public C getContext(Assignment<V, T> assignment) {
        if (iContext != null && assignment.getIndex() >= 0 && assignment.getIndex() < iContext.length) {
            AssignmentContext c = iContext[assignment.getIndex()];
            if (c != null) return (C)c;
        }
        return assignment.getAssignmentContext(getAssignmentContextReference());
    }

    @Override
    public AssignmentContextReference<V, T, C> getAssignmentContextReference() { return iContextReference; }

    @Override
    public void setAssignmentContextReference(AssignmentContextReference<V, T, C> reference) { iContextReference = reference; }

    @Override
    public AssignmentContext[] getContext() { return iContext; }

    @Override
    public void setContext(AssignmentContext[] context) { iContext = context; }
    
    @Override
    public void assigned(Assignment<V, T> assignment, long iteration, T value) {
        super.assigned(assignment, iteration, value);
        getContext(assignment).assigned(assignment, value);
    }
    
    @Override
    public void unassigned(Assignment<V, T> assignment, long iteration, T value) {
        super.unassigned(assignment, iteration, value);
        getContext(assignment).unassigned(assignment, value);
    }
}