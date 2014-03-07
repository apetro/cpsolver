package net.sf.cpsolver.ifs.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.sf.cpsolver.ifs.assignment.Assignment;
import net.sf.cpsolver.ifs.assignment.context.AssignmentConstraintContext;
import net.sf.cpsolver.ifs.assignment.context.AssignmentContext;
import net.sf.cpsolver.ifs.assignment.context.AssignmentContextReference;
import net.sf.cpsolver.ifs.assignment.context.CanHoldContext;
import net.sf.cpsolver.ifs.assignment.context.ConstraintWithContext;
import net.sf.cpsolver.ifs.assignment.context.HasAssignmentContext;
import net.sf.cpsolver.ifs.model.Constraint;
import net.sf.cpsolver.ifs.model.Model;
import net.sf.cpsolver.ifs.model.Value;
import net.sf.cpsolver.ifs.model.Variable;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.DataProperties;

/**
 * Abstract Criterion. <br>
 * <br>
 * An optimization objective can be split into several (optimization) criteria
 * and modeled as a weighted sum of these. This makes the implementation of a particular problem
 * more versatile as it allows for an easier modification of the optimization objective.
 * <br>
 * This class implements most of the {@link Criterion} except of the {@link Criterion#getValue(Assignment, Value, Set)}.
 * 
 * @version IFS 1.2 (Iterative Forward Search)<br>
 *          Copyright (C) 2006 - 2011 Tomas Muller<br>
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
public abstract class AbstractCriterion<V extends Variable<V, T>, T extends Value<V, T>> implements Criterion<V, T>, HasAssignmentContext<V, T, AbstractCriterion<V,T>.ValueContext>, CanHoldContext {
    private Model<V, T> iModel;
    protected double iBest = 0.0, iWeight = 0.0;
    protected static java.text.DecimalFormat sDoubleFormat = new java.text.DecimalFormat("0.##",
            new java.text.DecimalFormatSymbols(Locale.US));
    protected static java.text.DecimalFormat sPercentFormat = new java.text.DecimalFormat("0.##",
            new java.text.DecimalFormatSymbols(Locale.US));
    protected boolean iDebug = false;
    
    private AssignmentContextReference<V, T, ValueContext> iContextReference = null;
    private AssignmentContext[] iContext = null;

    
    /**
     * Defines how the overall value of the criterion should be automatically updated (using {@link Criterion#getValue(Value, Set)}).
     */
    protected static enum ValueUpdateType {
        /** Update is done before an unassignment (decrement) and before an assignment (increment). */
        BeforeUnassignedBeforeAssigned,
        /** Update is done after an unassignment (decrement) and before an assignment (increment). */
        AfterUnassignedBeforeAssigned,
        /** Update is done before an unassignment (decrement) and after an assignment (increment). */
        BeforeUnassignedAfterAssigned,
        /** Update is done after an unassignment (decrement) and after an assignment (increment). This is the default. */
        AfterUnassignedAfterAssigned,
        /** Criterion is to be updated manually (e.g., using {@link Criterion#inc(Assignment, double)}). */
        NoUpdate
    }
    protected ValueUpdateType iValueUpdateType = ValueUpdateType.BeforeUnassignedBeforeAssigned;

    /** Defines weight name (to be used to get the criterion weight from the configuration). */
    public String getWeightName() {
        return "Weight." + getClass().getName().substring(1 + getClass().getName().lastIndexOf('.'));
    }
    
    /** Defines default weight (when {@link AbstractCriterion#getWeightName()} parameter is not present in the criterion). */
    public double getWeightDefault(DataProperties config) {
        return 0.0;
    }
    
    @Override
    public void setModel(Model<V,T> model) {
        iModel = model;
        if (model != null)
            iContextReference = model.createReference(this);
    }

    @Override
    public boolean init(Solver<V, T> solver) {
        iWeight = solver.getProperties().getPropertyDouble(getWeightName(), getWeightDefault(solver.getProperties()));
        iDebug = solver.getProperties().getPropertyBoolean(
                "Debug." + getClass().getName().substring(1 + getClass().getName().lastIndexOf('.')),
                solver.getProperties().getPropertyBoolean("Debug.Criterion", false));
        return true;
    }
    
    /** Returns current model */
    public Model<V, T> getModel() { return iModel; }
    
    /**
     * Returns an assignment context associated with this criterion. If there is no 
     * assignment context associated with this criterion yet, one is created using the
     * {@link ConstraintWithContext#createAssignmentContext(Assignment)} method. From that time on,
     * this context is kept with the assignment and automatically updated by calling the
     * {@link AssignmentConstraintContext#assigned(Assignment, Value)} and {@link AssignmentConstraintContext#unassigned(Assignment, Value)}
     * whenever a variable is changed as given by the {@link ValueUpdateType}.
     * @param assignment given assignment
     * @return assignment context associated with this constraint and the given assignment
     */
    @SuppressWarnings("unchecked")
    public ValueContext getContext(Assignment<V, T> assignment) {
        if (iContext != null && assignment.getIndex() >= 0 && assignment.getIndex() < iContext.length) {
            AssignmentContext c = iContext[assignment.getIndex()];
            if (c != null) return (ValueContext) c;
        }
        return assignment.getAssignmentContext(getAssignmentContextReference());
    }
    
    @Override
    public ValueContext createAssignmentContext(Assignment<V,T> assignment) {
        return new ValueContext(assignment);
    }

    @Override
    public AssignmentContextReference<V, T, ValueContext> getAssignmentContextReference() { return iContextReference; }

    @Override
    public void setAssignmentContextReference(AssignmentContextReference<V, T, ValueContext> reference) { iContextReference = reference; }

    @Override
    public AssignmentContext[] getContext() {
        return iContext;
    }

    @Override
    public void setContext(AssignmentContext[] context) {
        iContext = context;
    }
    
    @Override
    public double getValue(Assignment<V, T> assignment) {
        return getContext(assignment).getTotal();
    }
    
    @Override
    public double getBest() {
        return iBest;
    }
    
    @Override
    public double getValue(Assignment<V, T> assignment, Collection<V> variables) {
        double ret = 0;
        for (V v: variables) {
            T t = assignment.getValue(v);
            if (t != null) ret += getValue(assignment, t, null);
        }
        return ret;
    }

    
    @Override
    public double getWeight() {
        return iWeight;
    }
    
    @Override
    public double getWeightedBest() {
        return getWeight() == 0.0 ? 0.0 : getWeight() * getBest();
    }
    
    @Override
    public double getWeightedValue(Assignment<V, T> assignment) {
        return (getWeight() == 0.0 ? 0.0 : getWeight() * getValue(assignment));
    }
    
    @Override
    public double getWeightedValue(Assignment<V, T> assignment, T value, Set<T> conflicts) {
        return (getWeight() == 0.0 ? 0.0 : getWeight() * getValue(assignment, value, conflicts));
    }
    
    @Override
    public double getWeightedValue(Assignment<V, T> assignment, Collection<V> variables) {
        return (getWeight() == 0.0 ? 0.0 : getWeight() * getValue(assignment, variables));
    }

    /** Compute bounds (bounds are being cached by default). */
    protected double[] computeBounds(Assignment<V, T> assignment) {
        return getBounds(assignment, new ArrayList<V>(getModel().variables()));
    }

    @Override
    public double[] getBounds(Assignment<V, T> assignment) {
        return getContext(assignment).getBounds(assignment);
    }

    @Override
    public double[] getBounds(Assignment<V, T> assignment, Collection<V> variables) {
        double[] bounds = new double[] { 0.0, 0.0 };
        for (V v: variables) {
            Double min = null, max = null;
            for (T t: v.values()) {
                double value = getValue(assignment, t, null);
                if (min == null) { min = value; max = value; continue; }
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
            if (min != null) {
                bounds[0] += min;
                bounds[1] += max;
            }
        }
        return bounds;
    }

    @Override
    public void beforeAssigned(Assignment<V, T> assignment, long iteration, T value) {
        switch (iValueUpdateType) {
            case AfterUnassignedBeforeAssigned:
            case BeforeUnassignedBeforeAssigned:
                getContext(assignment).assigned(assignment, value);
        }
    }

    @Override
    public void afterAssigned(Assignment<V, T> assignment, long iteration, T value) {
        switch (iValueUpdateType) {
            case AfterUnassignedAfterAssigned:
            case BeforeUnassignedAfterAssigned:
                getContext(assignment).assigned(assignment, value);
        }
    }

    @Override
    public void beforeUnassigned(Assignment<V, T> assignment, long iteration, T value) {
        switch (iValueUpdateType) {
            case BeforeUnassignedAfterAssigned:
            case BeforeUnassignedBeforeAssigned:
                getContext(assignment).unassigned(assignment, value);
        }
    }

    @Override
    public void afterUnassigned(Assignment<V, T> assignment, long iteration, T value) {
        switch (iValueUpdateType) {
            case AfterUnassignedAfterAssigned:
            case AfterUnassignedBeforeAssigned:
                getContext(assignment).unassigned(assignment, value);
        }
    }

    @Override
    public void bestSaved(Assignment<V, T> assignment) {
        iBest = getContext(assignment).getTotal();
    }

    @Override
    public void bestRestored(Assignment<V, T> assignment) {
        getContext(assignment).setTotal(iBest);
    }
    
    @Override
    public void inc(Assignment<V, T> assignment, double value) {
        getContext(assignment).inc(value);
    }   

    @Override
    public String getName() {
        return getClass().getName().substring(1 + getClass().getName().lastIndexOf('.')).replaceAll("(?<=[^A-Z])([A-Z])"," $1");
    }
    
    /** Clear bounds cache */
    protected void clearCache(Assignment<V, T> assignment) {
        getContext(assignment).setBounds(null);
    }
    
    @Override
    public void variableAdded(V variable) {
        // clearCache();
    }
    
    @Override
    public void variableRemoved(V variable) {
        // clearCache();
    }
    
    @Override
    public void constraintAdded(Constraint<V, T> constraint) {
        // clearCache();
    }
    
    @Override
    public void constraintRemoved(Constraint<V, T> constraint) {
        // clearCache();
    }
    
    protected String getPerc(double value, double min, double max) {
        if (max == min)
            return sPercentFormat.format(100.0);
        return sPercentFormat.format(100.0 - 100.0 * (value - min) / (max - min));
    }

    protected String getPercRev(double value, double min, double max) {
        if (max == min)
            return sPercentFormat.format(0.0);
        return sPercentFormat.format(100.0 * (value - min) / (max - min));
    }

    @Override
    public void getInfo(Assignment<V, T> assignment, Map<String, String> info) {
        if (iDebug) {
            double val = getValue(assignment), w = getWeightedValue(assignment), prec = getValue(assignment, getModel().variables());
            double[] bounds = getBounds(assignment);
            if (bounds[0] <= val && val <= bounds[1] && bounds[0] < bounds[1])
                info.put("[C] " + getName(),
                        getPerc(val, bounds[0], bounds[1]) + "% (value: " + sDoubleFormat.format(val) +
                        (Math.abs(prec - val) > 0.0001 ? ", precise:" + sDoubleFormat.format(prec) : "") +
                        ", weighted:" + sDoubleFormat.format(w) +
                        ", bounds: " + sDoubleFormat.format(bounds[0]) + ".." + sDoubleFormat.format(bounds[1]) + ")");
            else if (bounds[1] <= val && val <= bounds[0] && bounds[1] < bounds[0])
                info.put("[C] " + getName(),
                        getPercRev(val, bounds[1], bounds[0]) + "% (value: " + sDoubleFormat.format(val) +
                        (Math.abs(prec - val) > 0.0001 ? ", precise:" + sDoubleFormat.format(prec) : "") +
                        ", weighted:" + sDoubleFormat.format(w) +
                        ", bounds: " + sDoubleFormat.format(bounds[1]) + ".." + sDoubleFormat.format(bounds[0]) + ")");
            else if (bounds[0] != val || val != bounds[1])
                info.put("[C] " + getName(),
                        sDoubleFormat.format(val) + " (" +
                        (Math.abs(prec - val) > 0.0001 ? "precise:" + sDoubleFormat.format(prec) + ", ": "") +
                        "weighted:" + sDoubleFormat.format(w) +
                        (bounds[0] != bounds[1] ? ", bounds: " + sDoubleFormat.format(bounds[0]) + ".." + sDoubleFormat.format(bounds[1]) : "") +
                        ")");
        }
    }
    
    @Override
    public void getInfo(Assignment<V, T> assignment, Map<String, String> info, Collection<V> variables) {
        if (iDebug) {
            double val = getValue(assignment, variables), w = getWeightedValue(assignment, variables);
            double[] bounds = getBounds(assignment, variables);
            if (bounds[0] <= val && val <= bounds[1])
                info.put("[C] " + getName(),
                        getPerc(val, bounds[0], bounds[1]) + "% (value: " + sDoubleFormat.format(val) +
                        ", weighted:" + sDoubleFormat.format(w) +
                        ", bounds: " + sDoubleFormat.format(bounds[0]) + ".." + sDoubleFormat.format(bounds[1]) + ")");
            else if (bounds[1] <= val && val <= bounds[0])
                info.put("[C] " + getName(),
                        getPercRev(val, bounds[1], bounds[0]) + "% (value: " + sDoubleFormat.format(val) +
                        ", weighted:" + sDoubleFormat.format(w) +
                        ", bounds: " + sDoubleFormat.format(bounds[1]) + ".." + sDoubleFormat.format(bounds[0]) + ")");
            else if (bounds[0] != val || val != bounds[1])
                info.put("[C] " + getName(),
                        sDoubleFormat.format(val) + " (weighted:" + sDoubleFormat.format(w) +
                        (bounds[0] != bounds[1] ? ", bounds: " + sDoubleFormat.format(bounds[0]) + ".." + sDoubleFormat.format(bounds[1]) : "") +
                        ")");
        }
    }
    
    /**
     * Assignment context holding current value and the cached bounds.
     */
    public class ValueContext implements AssignmentContext {
        protected double iTotal = 0.0;
        private double[] iBounds = null;

        /** Create from an assignment */
        protected ValueContext(Assignment<V, T> assignment) {
            if (iValueUpdateType != ValueUpdateType.NoUpdate)
                iTotal = AbstractCriterion.this.getValue(assignment, assignment.assignedVariables());
        }
        
        /** Update value when unassigned */
        protected void unassigned(Assignment<V, T> assignment, T value) {
            iTotal -= getValue(assignment, value, null);
        }
        
        /** Update value when assigned */
        protected void assigned(Assignment<V, T> assignment, T value) {
            iTotal += getValue(assignment, value, null);
        }

        /** Return value */
        public double getTotal() { return iTotal; }
        
        /** Set value */
        public void setTotal(double value) { iTotal = value; }
        
        /** Increment value */
        public void inc(double value) { iTotal += value; }
        
        /** Return bounds */
        protected double[] getBounds(Assignment<V, T> assignment) {
            if (iBounds == null) iBounds = computeBounds(assignment);
            return (iBounds == null ? new double[] {0.0, 0.0} : iBounds);
        }
        
        /** Set bounds */
        protected void setBounds(double[] bounds) {
            iBounds = bounds;
        }
    }

    @Override
    @Deprecated
    public double getWeightedValue() {
        return getWeightedValue(getModel().getDefaultAssignment());
    }

    @Override
    @Deprecated
    public double[] getBounds() {
        return getBounds(getModel().getDefaultAssignment());
    }

    @Override
    @Deprecated
    public double getWeightedValue(T value, Set<T> conflicts) {
        return getWeightedValue(getModel().getDefaultAssignment(), value, conflicts);
    }
    
    @Override
    @Deprecated
    public double getValue(T value, Set<T> conflicts) {
        return getValue(getModel().getDefaultAssignment(), value, conflicts);
    }

    @Override
    @Deprecated
    public double getWeightedValue(Collection<V> variables) {
        return getWeightedValue(getModel().getDefaultAssignment(), variables);
    }

    @Override
    @Deprecated
    public double getValue(Collection<V> variables) {
        return getValue(getModel().getDefaultAssignment(), variables);
    }

    @Override
    @Deprecated
    public double[] getBounds(Collection<V> variables) {
        return getBounds(getModel().getDefaultAssignment(), variables);
    }
    
    @Override
    @Deprecated
    public void inc(double value) {
        inc(getModel().getDefaultAssignment(), value);
    }
}
