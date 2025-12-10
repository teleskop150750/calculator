class ExpressionEvaluator {
    constructor({functions, constants, operators}) {
        this.functions = functions
        this.constants = constants
        this.operators = operators
    }

    evaluate({rpn, variables = {}}) {
        const stack = []

        for (const token of rpn) {
            if (token.type === "number") {
                stack.push(parseFloat(token.value))
            }
            else if (token.type === "constant") {
                this.evaluateConstant(token, stack)
            }
            else if (token.type === "variable") {
                this.evaluateVariable(token, stack, variables)
            }
            else if (token.type === "operator") {
                this.evaluateOperator(token, stack)
            }
            else if (token.type === "function") {
                this.evaluateFunction(token, stack)
            }
            else
                throw new Error(`got invalid token "${token.value}" (${token.start}:${token.end})`)
        }

        if (stack.length != 1)
            throw new Error("expression is invalid")

        return stack[0]
    }

    evaluateConstant(token, stack) {
        if (!(token.value in this.constants))
            throw new Error(`got unknown constant "${token.value}" (${token.start}:${token.end})`)

        stack.push(this.constants[token.value])
    }

    evaluateVariable(token, stack, variables) {
        if (!(token.value in variables))
            throw new Error(`variable "${token.value}" value is not set`)

        stack.push(variables[token.value])
    }

    evaluateOperator(token, stack) {
        const operator = this.operators[token.value]

        if (!operator)
            throw new Error(`got unknown operator "${token.value}" (${token.start}:${token.end})`)

        if (stack.length < operator.args)
            throw new Error(`not enough arguments to evaluate operator "${token.value}" (${token.start}:${token.end})`)

        const args = stack.splice(-operator.args)
        stack.push(operator.evaluate(...args))
    }

    evaluateFunction(token, stack) {
        const func = this.functions[token.value]

        if (!func)
            throw new Error(`got unknown function "${token.value}" (${token.start}:${token.end})`)

        if (stack.length < func.args)
            throw new Error(`not enough arguments to evaluate function "${token.value}" (${token.start}:${token.end})`)

        const args = stack.splice(-func.args)
        stack.push(func.evaluate(...args))
    }
}
