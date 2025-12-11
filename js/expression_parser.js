class ExpressionParser {
    constructor(expression) {
        const functions = this.initFunctions()
        const constants = this.initConstans()
        const operators = this.initOperators()

        const tokenizer = new ExpressionTokenizer({
            functions: Object.keys(functions),
            constants: Object.keys(constants)
        })

        const parser = new PrattParser(functions)

        this.evaluator = new ExpressionEvaluator({constants, functions, operators})
        this.rpn = parser.parse(tokenizer.tokenize(expression))
        this.variables = {}
    }

    setVariable(name, value) {
        this.variables[name] = value
    }

    evaluate() {
        return this.evaluator.evaluate({rpn: this.rpn, variables: this.variables})
    }

    initFunctions() {        
        return {
            "sin": {args: 1, evaluate: Math.sin},
            "cos": {args: 1, evaluate: Math.cos},
            "tan": {args: 1, evaluate: Math.tan},
            "max": {args: 2, evaluate: Math.max},
        }
    }

    initConstans() {
        return {
            "pi": Math.PI,
            "e": Math.E
        }
    }

    initOperators() {
        return {
            "+": {args: 2, evaluate: (arg1, arg2) => arg1 + arg2},
            "-": {args: 2, evaluate: (arg1, arg2) => arg1 - arg2},
            "*": {args: 2, evaluate: (arg1, arg2) => arg1 * arg2},
            "/": {args: 2, evaluate: (arg1, arg2) => arg1 / arg2},
            "^": {args: 2, evaluate: (arg1, arg2) => arg1 ** arg2},
            "~": {args: 1, evaluate: (arg) => -arg}
        }
    }
}
