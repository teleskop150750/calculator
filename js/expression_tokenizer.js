class ExpressionTokenizer {
    constructor({functions, constants}) {
        this.regexp = new RegExp([
            `(?<left_parenthesis>\\()`,
            `(?<right_parenthesis>\\))`,
            `(?<delimeter>,)`,
            `(?<operator>[-+*/^])`,
            `(?<function>${functions.join("|")})`,
            `(?<constant>\\b(${constants.join("|")})\\b)`,
            `(?<number>\\d+(\\.\\d+)?)`,
            `(?<variable>[a-z]\\w*)`,
            `(?<unknown>\\S)`
        ].join("|"), "gi")
    }

    tokenize(expression) {
        const matches = expression.matchAll(this.regexp)
        const tokens = [...matches.map(match => this.matchToToken(match))]

        const unknown = tokens.filter(token => token.type === "unknown")
        if (unknown.length)
            throw new Error(`invalid tokens found: ${unknown.map(token => token.value).join(", ")}`)

        return tokens
    }

    matchToToken(match) {
        for (const [type, value] of Object.entries(match.groups))
            if (value)
                return {type: type, value: value, start: match.index, end: match.index + value.length}

        return null
    }
}
