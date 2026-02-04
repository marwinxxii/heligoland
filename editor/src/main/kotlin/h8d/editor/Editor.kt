package h8d.editor

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow

/**
 * Virtual editor for running code analysis and executing the program.
 */
public interface Editor {
    /**
     * State of editor, with analysis results.
     */
    public val state: StateFlow<State>

    /**
     * Update the state of the editor and trigger the analysis of the code.
     */
    public fun submitUpdate(code: String)

    public sealed interface State {
        public val text: String
        public val errors: List<Error>

        public sealed interface Error {
            public val lineNumber: UInt

            /**
             * Index of character in the line where the issue starts.
             */
            public val spanStart: UInt?

            /**
             * Index of character in the line where the issue starts.
             */
            public val spanEnd: UInt?

            public val message: String
        }
    }
}

public fun Editor(coroutineDispatcher: CoroutineDispatcher): Editor = EditorImpl(coroutineDispatcher)
