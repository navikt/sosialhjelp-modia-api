package no.nav.sbl.sosialhjelpmodiaapi.responses

val ok_digisossak_response_string = """
{
  "fiksDigisosId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "sokerFnr": "11111111111",
  "fiksOrgId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "kommunenummer": "1111",
  "sistEndret": 0,
  "originalSoknadNAV": {
    "navEksternRefId": "11000001",
    "metadata": "3fa85f64-5717-4562-b3fc-2c963f66afa0",
    "vedleggMetadata": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "soknadDokument": {
      "filnavn": "string",
      "dokumentlagerDokumentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "storrelse": 0
    },
    "vedlegg": [
      {
        "filnavn": "string",
        "dokumentlagerDokumentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "storrelse": 0
      }
    ],
    "timestampSendt": 0
  },
  "ettersendtInfoNAV": {
    "ettersendelser": [
      {
        "navEksternRefId": "navEksternRefIdEttersendelse0001",
        "vedleggMetadata": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "vedlegg": [
          {
            "filnavn": "string",
            "dokumentlagerDokumentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            "storrelse": 0
          }
        ],
        "timestampSendt": 0
      }
    ]
  },
  "digisosSoker": {
    "metadata": "3fa85f64-5717-4562-b3fc-2c963f66afa1",
    "dokumenter": [
      {
        "filnavn": "string",
        "dokumentlagerDokumentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "storrelse": 0
      }
    ],
    "timestampSistOppdatert": 0
  }
}
""".trimIndent()