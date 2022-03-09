package edu.coursera.concurrent;

import edu.coursera.concurrent.boruvka.Component;
import edu.coursera.concurrent.boruvka.Edge;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A parallel implementation of Boruvka's algorithm to compute a Minimum
 * Spanning Tree.
 */
public final class ParBoruvka extends AbstractBoruvka<ParBoruvka.ParComponent> {

    /**
     * Constructor.
     */
    public ParBoruvka() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void computeBoruvka(final Queue<ParComponent> nodesLoaded,
            final SolutionToBoruvka<ParComponent> solution) {
        ParComponent loopNode;

        while((loopNode = nodesLoaded.poll()) != null) {
            if (!loopNode.lock.tryLock()) {
                continue;  // Optimistic Concurrency :: Current polled node is locked ,
                // continue and poll another node
            }

            if (loopNode.isDead) {
                loopNode.lock.unlock();
                continue;
            }

            final Edge<ParComponent> edge = loopNode.getMinEdge();
            if (edge == null) {
                // No Edge  -- > all nodes merged to MST graph
                solution.setSolution(loopNode);
                break;
            }

            final ParComponent other = edge.getOther(loopNode);

            // Try to get lock on Other node so no other thread is also Merging with this Node
            if (!other.lock.tryLock()) {
                loopNode.lock.unlock();
                nodesLoaded.add(loopNode);
                continue;
            }
            // Acquired lock on Other node with Min weight
            if (other.isDead) {
                // Other is already Processed
                other.lock.unlock(); // Release Both Locks
                loopNode.lock.unlock();
                continue;
            }

            other.isDead = true;
            // Other Node was not Dead --> Both Nodes of edge are locked --> Merge
            loopNode.merge(other, edge.weight());

            //Release Resources
            loopNode.lock.unlock();
            other.lock.unlock();

            // Add merged Node to Queue
            nodesLoaded.add(loopNode);
        }
    }

    /**
     * ParComponent represents a single component in the graph. A component may
     * be a singleton representing a single node in the graph, or may be the
     * result of collapsing edges to form a component from multiple nodes.
     */
    public static final class ParComponent extends Component<ParComponent> {
        /**
         *  A unique identifier for this component in the graph that contains
         *  it.
         */
        public final int nodeId;

        /**
         * List of edges attached to this component, sorted by weight from least
         * to greatest.
         */
        private List<Edge<ParComponent>> edges = new ArrayList<>();

        /**
         * The weight this component accounts for. A component gains weight when
         * it is merged with another component across an edge with a certain
         * weight.
         */
        private double totalWeight = 0;

        /**
         * Number of edges that have been collapsed to create this component.
         */
        private long totalEdges = 0;

        /**
         * Whether this component has already been collapsed into another
         * component.
         */
        public boolean isDead = false;

        final  ReentrantLock lock = new ReentrantLock();

        /**
         * Constructor.
         *
         * @param setNodeId ID for this node.
         */
        public ParComponent(final int setNodeId) {
            super();
            this.nodeId = setNodeId;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int nodeId() {
            return nodeId;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double totalWeight() {
            return totalWeight;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long totalEdges() {
            return totalEdges;
        }

        /**
         * {@inheritDoc}
         *
         * Edge is inserted in weight order, from least to greatest.
         */
        public void addEdge(final Edge<ParComponent> e) {
            int i = 0;
            while (i < edges.size()) {
                if (e.weight() < edges.get(i).weight()) {
                    break;
                }
                i++;
            }
            edges.add(i, e);
        }

        /**
         * Get the edge with minimum weight from the sorted edge list.
         *
         * @return Edge with the smallest weight attached to this component.
         */
        public Edge<ParComponent> getMinEdge() {
            if (edges.size() == 0) {
                return null;
            }
            return edges.get(0);
        }

        /**
         * Merge two components together, connected by an edge with weight
         * edgeWeight.
         *
         * @param other The other component to merge into this component.
         * @param edgeWeight Weight of the edge connecting these components.
         */
        public void merge(final ParComponent other, final double edgeWeight) {
            totalWeight += other.totalWeight + edgeWeight;
            totalEdges += other.totalEdges + 1;

            final List<Edge<ParComponent>> newEdges = new ArrayList<>();
            int i = 0;
            int j = 0;
            while (i + j < edges.size() + other.edges.size()) {
                // Get rid of inter-component edges
                while (i < edges.size()) {
                    final Edge<ParComponent> e = edges.get(i);
                    if ((e.fromComponent() != this
                                && e.fromComponent() != other)
                            || (e.toComponent() != this
                                && e.toComponent() != other)) {
                        break;
                    }
                    i++;
                }
                while (j < other.edges.size()) {
                    final Edge<ParComponent> e = other.edges.get(j);
                    if ((e.fromComponent() != this
                                && e.fromComponent() != other)
                            || (e.toComponent() != this
                                && e.toComponent() != other)) {
                        break;
                    }
                    j++;
                }

                if (j < other.edges.size() && (i >= edges.size()
                            || edges.get(i).weight()
                            > other.edges.get(j).weight())) {
                    newEdges.add(other.edges.get(j++).replaceComponent(other,
                                this));
                } else if (i < edges.size()) {
                    newEdges.add(edges.get(i++).replaceComponent(other, this));
                }
            }
            other.edges.clear();
            edges.clear();
            edges = newEdges;
        }

        /**
         * Test for equality based on node ID.
         *
         * @param o Object to compare against.
         * @return true if they are the same component in the graph.
         */
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof Component)) {
                return false;
            }

            final Component component = (Component) o;
            return component.nodeId() == nodeId;
        }

        /**
         * Hash based on component node ID.
         *
         * @return Hash code.
         */
        @Override
        public int hashCode() {
            return nodeId;
        }
    }
    /* End ParComponent */

    /**
     * A ParEdge represents a weighted edge between two ParComponents.
     */
    public static final class ParEdge extends Edge<ParComponent>
            implements Comparable<Edge> {
        /**
         * Source component.
         */
        protected ParComponent fromComponent;

        /**
         * Destination component.
         */
        protected ParComponent toComponent;

        /**
         * Weight of this edge.
         */
        public double weight;

        /**
         * Constructor.
         *
         * @param from From edge.
         * @param to To edges.
         * @param w Weight of this edge.
         */
        public ParEdge(final ParComponent from, final ParComponent to,
                final double w) {
            fromComponent = from;
            toComponent = to;
            weight = w;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ParComponent fromComponent() {
            return fromComponent;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ParComponent toComponent() {
            return toComponent;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double weight() {
            return weight;
        }

        /**
         * {@inheritDoc}
         */
        public ParComponent getOther(final ParComponent from) {
            if (fromComponent == from) {
                assert (toComponent != from);
                return toComponent;
            }

            if (toComponent == from) {
                return fromComponent;
            }
            assert (false);
            return null;

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final Edge e) {
            return Double.compare(weight, e.weight());
        }

        /**
         * {@inheritDoc}
         */
        public ParEdge replaceComponent(final ParComponent from,
                final ParComponent to) {
            if (fromComponent == from) {
                fromComponent = to;
            }
            if (toComponent == from) {
                toComponent = to;
            }
            return this;
        }
    }
}
