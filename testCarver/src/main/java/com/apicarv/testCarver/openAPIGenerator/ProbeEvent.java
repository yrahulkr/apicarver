package com.apicarv.testCarver.openAPIGenerator;

import com.apicarv.testCarver.apirecorder.NetworkEvent;
import com.apicarv.testCarver.apirunner.APIResponse.Status;

public class ProbeEvent extends NetworkEvent {
	
	/**
	 * 	MDI2L- MergeDetectedIntermediateToLeaf
	 *  MDBpME - MergeDetectedBiPtMissingEdge
	 *  MDBpMN - MergeDetectedBiPtMissingNode
	 *  MOP -Missing Operation for a known path
	 */
	public enum ProbeType{
		/**
		 * MergeDetectedIntermediateToLeaf
		 */
		MDI2L, 
		/**
		 * MergeDetectedBiPtMissingEdge
		 */
		MDBpME, 
		/**
		 * MergeDetectedBiPtMissingNode
		 */
		MDBpMN,
		/**
		 * Missing Operation
		 */
		MOP,
		/**
		 * Response Analysis
		 */
		RA,
		Unknown
	}
	
	ProbeType probeType;
	
	private Status probeStatus;
	
	public ProbeType getProbeType() {
		return probeType;
	}

	public void setProbeType(ProbeType probeType) {
		this.probeType = probeType;
	}

	int priorityScore;
	
	public int getPriorityScore() {
		return priorityScore;
	}

	public void setPriorityScore(int priorityScore) {
		this.priorityScore = priorityScore;
	}

	URLNode mergeNode;
	
	public URLNode getMergeNode() {
		return mergeNode;
	}

	public void setMergeNode(URLNode mergeNode) {
		this.mergeNode = mergeNode;
	}

	public ProbeEvent(int id, URLNode mergeNode) {
		super(id);
		this.mergeNode = mergeNode;
	}

	public Status getProbeStatus() {
		return probeStatus;
	}

	public void setProbeStatus(Status probeStatus) {
		this.probeStatus = probeStatus;
	}
}
