// SPDX-FileCopyrightText: 2022 Pipeline Helper Contributors
//
// SPDX-License-Identifier: EUPL-1.2

public class InvalidFile {
    public static void main(String[] args) {
        System.out.println("This line is longer than 80 characters in this file");
    }

    public static void extraLongFunctionName(final Integer argumentOne, final Double argumentTwo) {
        System.out.println(argumentOne);
    }
}
