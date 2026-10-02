private applyNationalityChange(
  client: IPhysicalPerson,
  id: string,
  resolution: Resolution
): void {

  const previousCountry: string =
    id.substring('nationalities:'.length);

  const value: string | null =
    this.nullableStringValue(resolution.value);

  if (!value) {
    return;
  }

  // Initialise nationality si nécessaire
  if (!client.nationality) {
    client.nationality = {};
  }

  const nationality = client.nationality;

  // =========================================================
  // MANUAL ENTRY
  // nationalities:manual-1
  // =========================================================
  if (previousCountry.startsWith('manual-')) {

    const alreadyExists: boolean =
      nationality.first?.country === value ||
      nationality.second?.country === value ||
      nationality.third?.country === value;

    if (alreadyExists) {
      return;
    }

    if (!nationality.first) {
      nationality.first = { country: value } as NationalityInfo;
    } else if (!nationality.second) {
      nationality.second = { country: value } as NationalityInfo;
    } else if (!nationality.third) {
      nationality.third = { country: value } as NationalityInfo;
    }

    return;
  }

  // =========================================================
  // EXISTING ENTRY
  // ex: GB -> AL
  // =========================================================

  if (nationality.first?.country === previousCountry) {
    nationality.first.country = value;
    return;
  }

  if (nationality.second?.country === previousCountry) {
    nationality.second.country = value;
    return;
  }

  if (nationality.third?.country === previousCountry) {
    nationality.third.country = value;
    return;
  }

  console.warn(
    '[ClientProfilingChangeService] Nationality not found:',
    previousCountry,
    'client nationality:',
    structuredClone(nationality)
  );
}
