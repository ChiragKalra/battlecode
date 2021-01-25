package gen5.util;

import java.util.Iterator;


public class LinkedList<T> implements Iterable<T> {
    private Node head;
    private int size = 0;

    class Node {
        T data;
        Node next, prev;

        // Constructor
        Node (T d) {
            data = d;
            next = null;
            prev = null;
        }
    }

    public void add (T data) {
        size++;
        Node newNode = new Node(data);

        if (head != null) {
            head.prev = newNode;
            newNode.next = head;
        }
        head = newNode;
    }

    public int getSize() {
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return new ListIterator();
    }

    public class ListIterator implements Iterator<T> {
        Node curr;
        ListIterator () {
            curr = null;
        }

        @Override
        public boolean hasNext() {
            if (curr == null) return head != null;
            return curr.next != null;
        }

        @Override
        public T next() {
            if (curr == null) {
                curr = head;
            } else {
                curr = curr.next;
            }

            return curr.data;
        }

        @Override
        public void remove() {
            size--;
            Node prev = curr.prev, next = curr.next;
            if (prev != null) {
                prev.next = next;
            }
            if (next != null) {
                next.prev = prev;
            }
        }
    }

}
