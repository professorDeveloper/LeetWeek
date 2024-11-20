data class TreeNode(var left: Int = 0, var right: Int = 0)

fun swapNodes(v: Int, tree: Array<TreeNode>) {
    val left = tree[v].left
    val right = tree[v].right

    // Swap the left and right child nodes
    if (left > 0) tree[left].left = v
    if (right > 0) tree[right].right = v

    if (tree[v].left > 0) {
        tree[tree[v].left].right = right
    }

    if (tree[v].right > 0) {
        tree[tree[v].right].left = left
    }

    // Update the node's left and right pointers
    tree[v].left = left
    tree[v].right = right
}

fun inorder(v: Int, tree: Array<TreeNode>) {
    val stack = mutableListOf<Int>()
    var current = v

    while (stack.isNotEmpty() || current != 0) {
        if (current != 0) {
            // Push the current node onto the stack and move to the left child
            stack.add(current)
            current = tree[current].left
        } else {
            // If we reached a leaf node, pop from the stack and process it
            current = stack.removeAt(stack.size - 1)
            print("$current ")

            // Move to the right child
            current = tree[current].right
        }
    }
}

fun main() {
    val (N, Q) = readLine()!!.split(" ").map { it.toInt() }
    val tree = Array(N + 1) { TreeNode() }

    // Build the tree (binary tree) and ensure no invalid node connections
    for (i in 1..N / 2) {
        tree[i].left = 2 * i
        if (2 * i + 1 <= N) tree[i].right = 2 * i + 1
    }

    // Read the node swap operations
    val changes = readLine()!!.split(" ").map { it.toInt() }

    // Perform the node swaps and ensure tree structure is correct
    for (v in changes) {
        swapNodes(v, tree)
    }

    // Perform an inorder traversal and print the result
    inorder(1, tree)  // Start from the root node (1)
    println()          // Print a new line after the traversal
}
