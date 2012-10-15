package de.reneruck.tcd.ipp.database;

import java.util.LinkedHashSet;
import java.util.Set;

import de.reneruck.tcd.ipp.datamodel.Transition;
import de.reneruck.tcd.ipp.datamodel.TransitionState;

public class TemporalTransitionsStore {

	private Set<Transition> transitions = new LinkedHashSet<Transition>();
	
	public Transition getTransitionById(long transitionId) {
		for (Transition transition : this.transitions) {
			if(transition.getTransitionId() == transitionId)
			{
				return transition;
			}
		}
		return null;
	}
	
	public Set<Transition> getAllTransitionsByState(TransitionState state)
	{
		Set<Transition> result = new LinkedHashSet<Transition>();
		for (Transition transition : this.transitions) {
			if(state.equals(transition.getTransitionState()))
			{
				result.add(transition);
			}
		}
		return result;
	}
	
	public void addTransition(Transition transition) {
		this.transitions.add(transition);
	}
	
	public void removeTransition(Transition transition) {
		this.transitions.remove(transition);
	}
	
	public void removeTransitionById(long transitionId) {
		Transition transitionById = getTransitionById(transitionId);
		if(transitionById != null) {
			removeTransition(transitionById);
		}
	}

	public boolean containsTransition(Transition transition) {
		return this.transitions.contains(transition);
	}
}
